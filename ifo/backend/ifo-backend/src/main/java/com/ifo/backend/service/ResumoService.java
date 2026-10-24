package com.ifo.backend.service;

import com.ifo.backend.dto.DespesaGrupoResponse;
import com.ifo.backend.dto.ResumoResponse;
import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.TipoCategoria;
import com.ifo.backend.model.Transacao;
import com.ifo.backend.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

// Resumo do mês (RF08, RN04): saldo = receitas − despesas; investimentos contam como despesa
@Service
public class ResumoService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final TransacaoRepository repository;

    public ResumoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ResumoResponse resumir(Long idUsuario, YearMonth mes) {
        List<Transacao> transacoes = repository.findByUsuarioIdUsuarioAndDataBetweenOrderByDataDescIdTransacaoDesc(
                idUsuario, mes.atDay(1), mes.atEndOfMonth());

        BigDecimal totalReceitas = somar(transacoes, TipoCategoria.RECEITA, null);
        BigDecimal totalDespesas = somar(transacoes, TipoCategoria.DESPESA, null);

        // Sempre os três grupos, na ordem do enum, mesmo com total zero
        List<DespesaGrupoResponse> porGrupo = Arrays.stream(GrupoOrcamento.values())
                .map(grupo -> {
                    BigDecimal total = somar(transacoes, TipoCategoria.DESPESA, grupo);
                    return new DespesaGrupoResponse(grupo, total, percentual(total, totalDespesas));
                })
                .toList();

        return new ResumoResponse(mes.getMonthValue(), mes.getYear(), totalReceitas, totalDespesas,
                totalReceitas.subtract(totalDespesas), porGrupo);
    }

    // grupo null = soma todas as transações do tipo
    private BigDecimal somar(List<Transacao> transacoes, TipoCategoria tipo, GrupoOrcamento grupo) {
        return transacoes.stream()
                .filter(t -> t.getCategoria().getTipo() == tipo)
                .filter(t -> grupo == null || t.getCategoria().getGrupo() == grupo)
                .map(Transacao::getValor)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return parte.multiply(CEM).divide(total, 1, RoundingMode.HALF_UP);
    }
}
