package com.ifo.backend.service;

import com.ifo.backend.dto.DespesaGrupoResponse;
import com.ifo.backend.dto.ResumoResponse;
import com.ifo.backend.model.Categoria;
import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.TipoCategoria;
import com.ifo.backend.model.Transacao;
import com.ifo.backend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResumoServiceTest {

    private static final Long ID_USUARIO = 1L;
    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);

    private static final Categoria SALARIO = new Categoria((short) 1, "Salário", TipoCategoria.RECEITA, null);
    private static final Categoria ALIMENTACAO = new Categoria((short) 9, "Alimentação", TipoCategoria.DESPESA, GrupoOrcamento.NECESSIDADES);
    private static final Categoria DELIVERY = new Categoria((short) 14, "Delivery", TipoCategoria.DESPESA, GrupoOrcamento.LAZER);
    private static final Categoria RENDA_FIXA = new Categoria((short) 20, "Renda fixa", TipoCategoria.DESPESA, GrupoOrcamento.INVESTIMENTOS);

    private TransacaoRepository repository;
    private ResumoService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransacaoRepository.class);
        service = new ResumoService(repository);
    }

    // Exemplo do contrato da API (06-api.md §4)
    @Test
    void calculaTotaisSaldoEPercentualPorGrupo() {
        transacoesDoMes(
                transacao(SALARIO, "1500.00"),
                transacao(ALIMENTACAO, "500.00"),
                transacao(ALIMENTACAO, "200.00"),
                transacao(DELIVERY, "300.00"),
                transacao(RENDA_FIXA, "200.00"));

        ResumoResponse resumo = service.resumir(ID_USUARIO, OUTUBRO);

        assertEquals(10, resumo.mes());
        assertEquals(2026, resumo.ano());
        assertValor("1500.00", resumo.totalReceitas());
        assertValor("1200.00", resumo.totalDespesas());
        assertValor("300.00", resumo.saldo());
        assertGrupo(resumo.despesasPorGrupo().get(0), GrupoOrcamento.NECESSIDADES, "700.00", "58.3");
        assertGrupo(resumo.despesasPorGrupo().get(1), GrupoOrcamento.LAZER, "300.00", "25.0");
        assertGrupo(resumo.despesasPorGrupo().get(2), GrupoOrcamento.INVESTIMENTOS, "200.00", "16.7");
    }

    @Test
    void investimentoContaComoDespesaESaiDoSaldo() {
        transacoesDoMes(transacao(SALARIO, "1000.00"), transacao(RENDA_FIXA, "400.00"));

        ResumoResponse resumo = service.resumir(ID_USUARIO, OUTUBRO);

        assertValor("400.00", resumo.totalDespesas());
        assertValor("600.00", resumo.saldo());
    }

    @Test
    void saldoFicaNegativoQuandoDespesasPassamDasReceitas() {
        transacoesDoMes(transacao(SALARIO, "100.00"), transacao(DELIVERY, "150.50"));

        assertValor("-50.50", service.resumir(ID_USUARIO, OUTUBRO).saldo());
    }

    @Test
    void mesSemTransacoesDevolveZerosEOsTresGrupos() {
        transacoesDoMes();

        ResumoResponse resumo = service.resumir(ID_USUARIO, OUTUBRO);

        assertValor("0.00", resumo.totalReceitas());
        assertValor("0.00", resumo.totalDespesas());
        assertValor("0.00", resumo.saldo());
        assertEquals(3, resumo.despesasPorGrupo().size());
        resumo.despesasPorGrupo().forEach(g -> {
            assertValor("0.00", g.total());
            assertValor("0.0", g.percentual());
        });
    }

    @Test
    void soReceitasNaoDivideAsDespesasPorZero() {
        transacoesDoMes(transacao(SALARIO, "2000.00"));

        ResumoResponse resumo = service.resumir(ID_USUARIO, OUTUBRO);

        assertValor("2000.00", resumo.saldo());
        resumo.despesasPorGrupo().forEach(g -> assertValor("0.0", g.percentual()));
    }

    @Test
    void buscaSoAsTransacoesDoMesPedido() {
        transacoesDoMes();

        service.resumir(ID_USUARIO, YearMonth.of(2024, 2));

        verify(repository).findByUsuarioIdUsuarioAndDataBetweenOrderByDataDescIdTransacaoDesc(
                ID_USUARIO, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));
    }

    private void transacoesDoMes(Transacao... transacoes) {
        when(repository.findByUsuarioIdUsuarioAndDataBetweenOrderByDataDescIdTransacaoDesc(any(), any(), any()))
                .thenReturn(List.of(transacoes));
    }

    private Transacao transacao(Categoria categoria, String valor) {
        Transacao transacao = new Transacao();
        transacao.setCategoria(categoria);
        transacao.setValor(new BigDecimal(valor));
        transacao.setData(OUTUBRO.atDay(10));
        return transacao;
    }

    private void assertGrupo(DespesaGrupoResponse grupo, GrupoOrcamento esperado, String total, String percentual) {
        assertEquals(esperado, grupo.grupo());
        assertValor(total, grupo.total());
        assertValor(percentual, grupo.percentual());
    }

    // Compara valor e casas decimais (ex.: "0.00" ≠ "0")
    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(new BigDecimal(esperado), atual);
    }
}
