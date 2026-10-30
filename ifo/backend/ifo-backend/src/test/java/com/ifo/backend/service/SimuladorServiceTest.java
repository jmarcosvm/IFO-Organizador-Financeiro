package com.ifo.backend.service;

import com.ifo.backend.dto.EvolucaoMesResponse;
import com.ifo.backend.dto.SimulacaoRequest;
import com.ifo.backend.dto.SimulacaoResponse;
import com.ifo.backend.model.TipoJuros;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SimuladorServiceTest {

    private final SimuladorService service = new SimuladorService();

    // Caso de referência do contrato (06-api.md §7)
    @Test
    void compostoDeReferenciaDa3804e75() {
        SimulacaoResponse resultado = service.simular(pedido(null, "300.00", "1.0", 12, TipoJuros.COMPOSTO));

        assertValor("3804.75", resultado.montanteFinal());
        assertValor("3600.00", resultado.totalInvestido());
        assertValor("204.75", resultado.rendimento());
    }

    @Test
    void evolucaoTemUmaLinhaPorMesEBatecomOResultadoFinal() {
        SimulacaoResponse resultado = service.simular(pedido(null, "300.00", "1.0", 12, TipoJuros.COMPOSTO));

        assertEquals(12, resultado.evolucao().size());
        EvolucaoMesResponse primeiro = resultado.evolucao().get(0);
        assertEquals(1, primeiro.mes());
        assertValor("300.00", primeiro.investido());
        assertValor("300.00", primeiro.montante());
        EvolucaoMesResponse ultimo = resultado.evolucao().get(11);
        assertEquals(12, ultimo.mes());
        assertValor("3804.75", ultimo.montante());
    }

    @Test
    void compostoSoComAplicacaoInicial() {
        // 1000 × 1,01^12 = 1126,825...
        SimulacaoResponse resultado = service.simular(pedido("1000.00", "0", "1.0", 12, TipoJuros.COMPOSTO));

        assertValor("1126.83", resultado.montanteFinal());
        assertValor("1000.00", resultado.totalInvestido());
        assertValor("126.83", resultado.rendimento());
    }

    @Test
    void jurosSimplesUsaAFormulaDaRN13() {
        // 1000·(1+0,12) + 100·12 + 100·0,01·12·11/2 = 1120 + 1200 + 66
        SimulacaoResponse resultado = service.simular(pedido("1000.00", "100.00", "1.0", 12, TipoJuros.SIMPLES));

        assertValor("2386.00", resultado.montanteFinal());
        assertValor("2200.00", resultado.totalInvestido());
        assertValor("186.00", resultado.rendimento());
    }

    @Test
    void taxaZeroNaoDividePorZeroERendeZero() {
        SimulacaoResponse resultado = service.simular(pedido("1000", "100", "0", 12, TipoJuros.COMPOSTO));

        assertValor("2200.00", resultado.montanteFinal());
        assertValor("0.00", resultado.rendimento());
    }

    @Test
    void aplicacaoInicialVaziaContaComoZero() {
        SimulacaoResponse resultado = service.simular(pedido(null, "100.00", "0", 3, TipoJuros.SIMPLES));

        assertValor("300.00", resultado.totalInvestido());
    }

    @Test
    void umMesApenasRendeUmaVezATaxa() {
        SimulacaoResponse resultado = service.simular(pedido("1000.00", "0", "2.0", 1, TipoJuros.COMPOSTO));

        assertValor("1020.00", resultado.montanteFinal());
        assertValor("20.00", resultado.rendimento());
    }

    @Test
    void prazoMaximoNaoEstouraPrecisao() {
        SimulacaoResponse resultado = service.simular(pedido("1000.00", "100.00", "1.0", 1200, TipoJuros.COMPOSTO));

        assertEquals(1200, resultado.evolucao().size());
        assertValor("121000.00", resultado.totalInvestido());
        assertTrue(resultado.montanteFinal().compareTo(resultado.totalInvestido()) > 0);
    }

    private SimulacaoRequest pedido(String inicial, String mensal, String taxa, int meses, TipoJuros tipo) {
        return new SimulacaoRequest(inicial != null ? new BigDecimal(inicial) : null, new BigDecimal(mensal),
                new BigDecimal(taxa), meses, tipo);
    }

    // Compara valor e casas decimais (ex.: "0.00" ≠ "0")
    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(new BigDecimal(esperado), atual);
    }
}
