package com.ifo.backend.service;

import com.ifo.backend.dto.AlertaOrcamentoResponse;
import com.ifo.backend.dto.DespesaGrupoResponse;
import com.ifo.backend.dto.OrcamentoRequest;
import com.ifo.backend.dto.OrcamentoResponse;
import com.ifo.backend.dto.StatusGrupoResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.exception.RegraNegocioException;
import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.NivelOrcamento;
import com.ifo.backend.model.Orcamento;
import com.ifo.backend.model.Usuario;
import com.ifo.backend.repository.OrcamentoRepository;
import com.ifo.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Orçamento 50/30/20 (RF11–RF14, RN05–RN08)
@Service
public class OrcamentoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal LIMIAR_ATENCAO = new BigDecimal("0.80");

    private static final Map<GrupoOrcamento, BigDecimal> PADRAO = new EnumMap<>(Map.of(
            GrupoOrcamento.NECESSIDADES, BigDecimal.valueOf(50),
            GrupoOrcamento.LAZER, BigDecimal.valueOf(30),
            GrupoOrcamento.INVESTIMENTOS, BigDecimal.valueOf(20)));

    private final OrcamentoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final ResumoService resumoService;

    public OrcamentoService(OrcamentoRepository repository, UsuarioRepository usuarioRepository,
                            ResumoService resumoService) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.resumoService = resumoService;
    }

    // Ordem fixa: Necessidades, Lazer, Investimentos
    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listar(Long idUsuario) {
        return repository.findByUsuarioIdUsuario(idUsuario).stream()
                .sorted(Comparator.comparing(Orcamento::getGrupo))
                .map(OrcamentoResponse::from)
                .toList();
    }

    // Chamado pelo cadastro (RF11): todo usuário novo nasce com 50/30/20
    @Transactional
    public void criarPadrao(Usuario usuario) {
        PADRAO.forEach((grupo, percentual) -> repository.save(novo(usuario, grupo, percentual)));
    }

    @Transactional
    public List<OrcamentoResponse> atualizar(Long idUsuario, OrcamentoRequest request) {
        Map<GrupoOrcamento, BigDecimal> novos = new EnumMap<>(GrupoOrcamento.class);
        novos.put(GrupoOrcamento.NECESSIDADES, request.necessidades());
        novos.put(GrupoOrcamento.LAZER, request.lazer());
        novos.put(GrupoOrcamento.INVESTIMENTOS, request.investimentos());

        BigDecimal soma = novos.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (soma.compareTo(CEM) != 0) {
            throw new RegraNegocioException("A soma dos percentuais deve ser 100.");
        }

        Map<GrupoOrcamento, Orcamento> atuais = repository.findByUsuarioIdUsuario(idUsuario).stream()
                .collect(Collectors.toMap(Orcamento::getGrupo, Function.identity()));
        Usuario usuario = usuarioRepository.getReferenceById(idUsuario);

        // Se faltar alguma linha (usuário antigo), cria; senão só ajusta o percentual
        novos.forEach((grupo, percentual) -> {
            Orcamento orcamento = atuais.getOrDefault(grupo, novo(usuario, grupo, percentual));
            orcamento.setPercentual(percentual);
            repository.save(orcamento);
        });
        return listar(idUsuario);
    }

    @Transactional(readOnly = true)
    public List<StatusGrupoResponse> status(Long idUsuario, YearMonth mes) {
        BigDecimal renda = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."))
                .getRendaMensal();

        // Reaproveita o resumo do mês em vez de somar as despesas de novo
        Map<GrupoOrcamento, BigDecimal> gastos = resumoService.resumir(idUsuario, mes).despesasPorGrupo().stream()
                .collect(Collectors.toMap(DespesaGrupoResponse::grupo, DespesaGrupoResponse::total));

        return listar(idUsuario).stream()
                .map(o -> calcularStatus(o.grupo(), o.percentual(), renda,
                        gastos.getOrDefault(o.grupo(), BigDecimal.ZERO)))
                .toList();
    }

    // Aviso para uma despesa recém-lançada; null se for receita (grupo null) ou se está tudo OK (RN08)
    @Transactional(readOnly = true)
    public AlertaOrcamentoResponse alertar(Long idUsuario, GrupoOrcamento grupo, YearMonth mes) {
        if (grupo == null) {
            return null;
        }
        return status(idUsuario, mes).stream()
                .filter(s -> s.grupo() == grupo)
                .filter(s -> s.nivel() == NivelOrcamento.ATENCAO || s.nivel() == NivelOrcamento.ESTOURO)
                .findFirst()
                .map(s -> new AlertaOrcamentoResponse(s.grupo(), s.nivel(), s.percentualUsado(), s.limite(),
                        s.gasto(), mensagem(s)))
                .orElse(null);
    }

    // Limite = renda × percentual (RN06); sem renda não há limite nem aviso
    static StatusGrupoResponse calcularStatus(GrupoOrcamento grupo, BigDecimal percentual, BigDecimal renda,
                                              BigDecimal gasto) {
        if (renda.signum() == 0) {
            return new StatusGrupoResponse(grupo, percentual, null, gasto, null, NivelOrcamento.SEM_LIMITE);
        }
        BigDecimal limite = renda.multiply(percentual).divide(CEM, 2, RoundingMode.HALF_UP);
        BigDecimal percentualUsado = limite.signum() == 0 ? null
                : gasto.multiply(CEM).divide(limite, 1, RoundingMode.HALF_UP);
        return new StatusGrupoResponse(grupo, percentual, limite, gasto, percentualUsado, nivel(gasto, limite));
    }

    // Compara os valores em reais (e não o % já arredondado), senão 100,01% viraria 100,0% e não estouraria
    private static NivelOrcamento nivel(BigDecimal gasto, BigDecimal limite) {
        if (limite.signum() == 0) {
            return gasto.signum() > 0 ? NivelOrcamento.ESTOURO : NivelOrcamento.OK;
        }
        if (gasto.compareTo(limite) > 0) {
            return NivelOrcamento.ESTOURO;
        }
        if (gasto.compareTo(limite.multiply(LIMIAR_ATENCAO)) >= 0) {
            return NivelOrcamento.ATENCAO;
        }
        return NivelOrcamento.OK;
    }

    private static String mensagem(StatusGrupoResponse status) {
        String rotulo = switch (status.grupo()) {
            case NECESSIDADES -> "Necessidades";
            case LAZER -> "Lazer";
            case INVESTIMENTOS -> "Investimentos";
        };
        if (status.nivel() == NivelOrcamento.ESTOURO) {
            return "Você ultrapassou o limite de " + rotulo + " deste mês.";
        }
        String usado = status.percentualUsado().toPlainString().replace('.', ',');
        return "Você já usou " + usado + "% do limite de " + rotulo + " deste mês.";
    }

    private static Orcamento novo(Usuario usuario, GrupoOrcamento grupo, BigDecimal percentual) {
        Orcamento orcamento = new Orcamento();
        orcamento.setUsuario(usuario);
        orcamento.setGrupo(grupo);
        orcamento.setPercentual(percentual);
        return orcamento;
    }
}
