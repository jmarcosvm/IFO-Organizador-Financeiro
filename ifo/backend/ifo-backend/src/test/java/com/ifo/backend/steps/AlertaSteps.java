package com.ifo.backend.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

// O alerta de orçamento (RN07/RN08) vem junto da resposta de POST /api/transacoes e nunca bloqueia
public class AlertaSteps {

    @Autowired
    private ApiCliente api;

    @E("não deve haver alerta de orçamento")
    public void sem_alerta() {
        assertTrue(api.corpo().path("alerta").isNull(), "Alerta inesperado: " + api.corpo().path("alerta"));
    }

    @E("deve haver alerta de orçamento {string} para o grupo {string}")
    public void com_alerta(String nivel, String grupo) {
        JsonNode alerta = api.corpo().path("alerta");
        assertFalse(alerta.isNull() || alerta.isMissingNode(), "Era esperado um alerta de " + nivel);
        assertEquals(nivel, alerta.path("nivel").asText());
        assertEquals(grupo, alerta.path("grupo").asText());
        assertFalse(alerta.path("mensagem").asText().isBlank());
    }
}
