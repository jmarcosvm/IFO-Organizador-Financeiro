package com.ifo.backend.steps;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Simulador de investimentos (RN13/ADR-003): só calcula, não grava nada
public class SimuladorSteps {

    @Autowired
    private ApiCliente api;

    @Quando("eu simulo aporte mensal de {bigdecimal} a {bigdecimal}% ao mês por {int} meses com juros {string}")
    public void simular(BigDecimal mensal, BigDecimal taxa, int meses, String tipoJuros) {
        simular_com_inicial(BigDecimal.ZERO, mensal, taxa, meses, tipoJuros);
    }

    @Quando("eu simulo aplicação inicial de {bigdecimal} com aporte mensal de {bigdecimal} a {bigdecimal}% ao mês por {int} meses com juros {string}")
    public void simular_com_inicial(BigDecimal inicial, BigDecimal mensal, BigDecimal taxa, int meses, String tipoJuros) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("aplicacaoInicial", inicial);
        corpo.put("valorMensal", mensal);
        corpo.put("taxaMensal", taxa);
        corpo.put("tempoMeses", meses);
        corpo.put("tipoJuros", tipoJuros);
        api.post("/api/investimentos/simular", corpo);
    }

    @Entao("o montante final deve ser {bigdecimal}, o total investido {bigdecimal} e o rendimento {bigdecimal}")
    public void conferir(BigDecimal montante, BigDecimal investido, BigDecimal rendimento) {
        assertEquals(200, api.status(), "Corpo da resposta: " + api.corpo());
        assertEquals(0, api.corpo().path("montanteFinal").decimalValue().compareTo(montante),
                "montanteFinal = " + api.corpo().path("montanteFinal"));
        assertEquals(0, api.corpo().path("totalInvestido").decimalValue().compareTo(investido));
        assertEquals(0, api.corpo().path("rendimento").decimalValue().compareTo(rendimento));
    }

    @Entao("a evolução deve ter {int} meses")
    public void evolucao(int meses) {
        assertEquals(meses, api.corpo().path("evolucao").size());
    }
}
