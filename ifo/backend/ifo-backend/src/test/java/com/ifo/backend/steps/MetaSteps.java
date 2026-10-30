package com.ifo.backend.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Metas pela API: criação, aportes (RN09) e isolamento entre usuários (RN11)
public class MetaSteps {

    @Autowired
    private ApiCliente api;

    private long idMeta;

    @Dado("que eu criei a meta {string} com objetivo de {bigdecimal}")
    @Quando("eu crio a meta {string} com objetivo de {bigdecimal}")
    public void criar_meta(String titulo, BigDecimal objetivo) {
        api.post("/api/metas", meta(titulo, objetivo));
        if (api.status() == 201) {
            idMeta = api.corpo().path("idMeta").asLong();
        }
    }

    @Quando("eu guardo {bigdecimal} nessa meta")
    @Dado("que eu guardei {bigdecimal} nessa meta")
    public void aportar(BigDecimal valor) {
        api.post("/api/metas/" + idMeta + "/aportes", Map.of("valor", valor));
        assertEquals(200, api.status(), "Aporte falhou: " + api.corpo());
    }

    @Entao("a meta deve ter {bigdecimal} guardados e {bigdecimal}% de progresso")
    public void conferir_progresso(BigDecimal guardado, BigDecimal progresso) {
        assertEquals(0, api.corpo().path("valorAtual").decimalValue().compareTo(guardado));
        assertEquals(0, api.corpo().path("progresso").decimalValue().compareTo(progresso));
    }

    @Entao("a meta deve estar concluída")
    public void concluida() {
        assertTrue(api.corpo().path("concluida").asBoolean());
    }

    @Entao("a meta não deve estar concluída")
    public void nao_concluida() {
        assertFalse(api.corpo().path("concluida").asBoolean());
    }

    @Quando("outro usuário tenta guardar {bigdecimal} nessa meta")
    public void outro_usuario_aporta(BigDecimal valor) {
        api.comOutroUsuario(() -> api.post("/api/metas/" + idMeta + "/aportes", Map.of("valor", valor)));
    }

    @Quando("outro usuário tenta excluir essa meta")
    public void outro_usuario_exclui() {
        api.comOutroUsuario(() -> api.delete("/api/metas/" + idMeta));
    }

    @Entao("a meta continua existindo para mim com {bigdecimal} guardados")
    public void meta_intacta(BigDecimal guardado) {
        api.get("/api/metas");
        assertEquals(1, api.corpo().size());
        assertEquals(0, api.corpo().get(0).path("valorAtual").decimalValue().compareTo(guardado));
    }

    private Map<String, Object> meta(String titulo, BigDecimal objetivo) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("tituloMeta", titulo);
        corpo.put("valorObjetivo", objetivo);
        return corpo;
    }
}
