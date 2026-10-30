package com.ifo.backend.service;

import com.ifo.backend.dto.RecorrenciaRequest;
import com.ifo.backend.dto.RecorrenciaResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.exception.RegraNegocioException;
import com.ifo.backend.model.Categoria;
import com.ifo.backend.model.TipoCategoria;
import com.ifo.backend.model.Transacao;
import com.ifo.backend.model.TransacaoRecorrente;
import com.ifo.backend.repository.TransacaoRecorrenteRepository;
import com.ifo.backend.repository.TransacaoRepository;
import com.ifo.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

// Recorrências (RF20, RN14–RN17). Toda operação filtra pelo usuário do token (RN11)
@Service
public class RecorrenciaService {

    private final TransacaoRecorrenteRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;

    public RecorrenciaService(TransacaoRecorrenteRepository repository, TransacaoRepository transacaoRepository,
                              UsuarioRepository usuarioRepository, CategoriaService categoriaService) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<RecorrenciaResponse> listar(Long idUsuario) {
        return repository.findByUsuarioIdUsuarioOrderByIdRecorrenciaAsc(idUsuario).stream()
                .map(RecorrenciaResponse::from)
                .toList();
    }

    // Ao criar, já gera as ocorrências vencidas (RN15)
    @Transactional
    public RecorrenciaResponse criar(Long idUsuario, RecorrenciaRequest request) {
        TransacaoRecorrente recorrencia = new TransacaoRecorrente();
        recorrencia.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        preencher(recorrencia, request);
        repository.save(recorrencia);
        gerar(recorrencia, LocalDate.now());
        return RecorrenciaResponse.from(recorrencia);
    }

    // As transações já geradas não mudam (RN16): a edição só vale para os próximos meses
    @Transactional
    public RecorrenciaResponse atualizar(Long idUsuario, Long idRecorrencia, RecorrenciaRequest request) {
        TransacaoRecorrente recorrencia = buscarDoUsuario(idUsuario, idRecorrencia);
        preencher(recorrencia, request);
        gerar(recorrencia, LocalDate.now());
        return RecorrenciaResponse.from(recorrencia);
    }

    @Transactional
    public RecorrenciaResponse definirAtiva(Long idUsuario, Long idRecorrencia, boolean ativa) {
        TransacaoRecorrente recorrencia = buscarDoUsuario(idUsuario, idRecorrencia);
        boolean estavaPausada = !recorrencia.isAtiva();
        recorrencia.setAtiva(ativa);
        if (ativa && estavaPausada) {
            pularMesesPausados(recorrencia, LocalDate.now());
            gerar(recorrencia, LocalDate.now());
        }
        return RecorrenciaResponse.from(recorrencia);
    }

    // As transações geradas ficam; o banco zera o vínculo delas (ON DELETE SET NULL, RN16)
    @Transactional
    public void excluir(Long idUsuario, Long idRecorrencia) {
        repository.delete(buscarDoUsuario(idUsuario, idRecorrencia));
    }

    // Usado pelo job: cada recorrência numa transação própria, para uma falha não derrubar as outras
    @Transactional
    public int gerarPorId(Long idRecorrencia, LocalDate hoje) {
        return repository.findById(idRecorrencia).map(r -> gerar(r, hoje)).orElse(0);
    }

    public List<Long> idsAtivas() {
        return repository.findIdsAtivas();
    }

    // Avança mês a mês da última competência até o mês de "hoje", sem duplicar (RN15).
    // Devolve quantas transações criou.
    int gerar(TransacaoRecorrente recorrencia, LocalDate hoje) {
        if (!recorrencia.isAtiva()) {
            return 0;
        }
        YearMonth atual = YearMonth.from(hoje);
        YearMonth mes = recorrencia.getUltimaCompetencia() == null
                ? YearMonth.from(recorrencia.getDataInicio())
                : YearMonth.from(recorrencia.getUltimaCompetencia()).plusMonths(1);
        int geradas = 0;
        for (; !mes.isAfter(atual); mes = mes.plusMonths(1)) {
            // Mês curto usa o último dia (RN14): dia 31 em fevereiro vira 28 ou 29
            LocalDate ocorrencia = mes.atDay(Math.min(recorrencia.getDiaMes(), mes.lengthOfMonth()));
            if (ocorrencia.isAfter(hoje)) {
                break; // ainda não chegou; o job tenta de novo amanhã
            }
            if (recorrencia.getDataFim() != null && ocorrencia.isAfter(recorrencia.getDataFim())) {
                break; // passou do fim (RN17)
            }
            if (!ocorrencia.isBefore(recorrencia.getDataInicio())) {
                transacaoRepository.save(novaTransacao(recorrencia, ocorrencia));
                geradas++;
            }
            recorrencia.setUltimaCompetencia(mes.atDay(1));
        }
        return geradas;
    }

    // Reativar não "paga" os meses em que esteve pausada (RN17): recomeça do mês atual
    private void pularMesesPausados(TransacaoRecorrente recorrencia, LocalDate hoje) {
        YearMonth proximo = recorrencia.getUltimaCompetencia() == null
                ? YearMonth.from(recorrencia.getDataInicio())
                : YearMonth.from(recorrencia.getUltimaCompetencia()).plusMonths(1);
        YearMonth atual = YearMonth.from(hoje);
        if (proximo.isBefore(atual)) {
            recorrencia.setUltimaCompetencia(atual.minusMonths(1).atDay(1));
        }
    }

    private Transacao novaTransacao(TransacaoRecorrente recorrencia, LocalDate data) {
        Transacao transacao = new Transacao();
        transacao.setUsuario(recorrencia.getUsuario());
        transacao.setCategoria(recorrencia.getCategoria());
        transacao.setRecorrencia(recorrencia);
        transacao.setValor(recorrencia.getValor());
        transacao.setData(data);
        transacao.setDescricao(recorrencia.getDescricao());
        return transacao;
    }

    private void preencher(TransacaoRecorrente recorrencia, RecorrenciaRequest request) {
        Categoria categoria = categoriaService.buscarEntidade(request.idCategoria());
        if (categoria.getTipo() != TipoCategoria.DESPESA) {
            throw new RegraNegocioException("Só categorias de despesa podem ser recorrentes.");
        }
        if (request.dataFim() != null && request.dataFim().isBefore(request.dataInicio())) {
            throw new RegraNegocioException("A data final não pode ser anterior à data de início.");
        }
        recorrencia.setCategoria(categoria);
        recorrencia.setValor(request.valor());
        recorrencia.setDescricao(request.descricao());
        recorrencia.setDiaMes(request.diaMes());
        recorrencia.setDataInicio(request.dataInicio());
        recorrencia.setDataFim(request.dataFim());
    }

    // Recorrência de outro usuário responde igual a inexistente (404)
    private TransacaoRecorrente buscarDoUsuario(Long idUsuario, Long idRecorrencia) {
        return repository.findByIdRecorrenciaAndUsuarioIdUsuario(idRecorrencia, idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recorrência não encontrada."));
    }
}
