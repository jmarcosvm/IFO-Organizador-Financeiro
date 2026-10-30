package com.ifo.backend.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

// Isolamento entre usuários (RN11): o que um usuário cria, o outro não vê nem altera.
// "Outro usuário" é sempre um usuário recém-cadastrado, com o próprio token.
public class IsolamentoSteps {

    @Autowired
    private ApiCliente api;

    private long idRecorrencia;

    // ---- recorrências ----

    @Dado("que eu criei uma despesa recorrente de {bigdecimal} na categoria {string}")
    public void criar_recorrencia(BigDecimal valor, String categoria) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("idCategoria", api.idCategoria(categoria));
        corpo.put("valor", valor);
        corpo.put("descricao", "Recorrência de teste Cucumber");
        corpo.put("diaMes", 1);
        corpo.put("dataInicio", LocalDate.now().toString());
        api.post("/api/recorrencias", corpo);
        assertEquals(201, api.status(), "Criação falhou: " + api.corpo());
        idRecorrencia = api.corpo().path("idRecorrencia").asLong();
    }

    @Quando("outro usuário tenta editar essa recorrência para {bigdecimal}")
    public void outro_usuario_edita_recorrencia(BigDecimal valor) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("idCategoria", api.idCategoria("Contas fixas"));
        corpo.put("valor", valor);
        corpo.put("diaMes", 1);
        corpo.put("dataInicio", LocalDate.now().toString());
        api.comOutroUsuario(() -> api.put("/api/recorrencias/" + idRecorrencia, corpo));
    }

    @Quando("outro usuário tenta pausar essa recorrência")
    public void outro_usuario_pausa_recorrencia() {
        api.comOutroUsuario(() -> api.enviar(org.springframework.http.HttpMethod.PATCH,
                "/api/recorrencias/" + idRecorrencia + "/ativa", Map.of("ativa", false)));
    }

    @Quando("outro usuário tenta excluir essa recorrência")
    public void outro_usuario_exclui_recorrencia() {
        api.comOutroUsuario(() -> api.delete("/api/recorrencias/" + idRecorrencia));
    }

    @Entao("a recorrência continua ativa para mim com valor {bigdecimal}")
    public void recorrencia_intacta(BigDecimal valor) {
        api.get("/api/recorrencias");
        assertEquals(1, api.corpo().size());
        assertTrue(api.corpo().get(0).path("ativa").asBoolean());
        assertEquals(0, api.corpo().get(0).path("valor").decimalValue().compareTo(valor));
    }

    // ---- listas, resumo, CSV e orçamento ----

    @Quando("outro usuário consulta {string}")
    public void outro_usuario_consulta(String rota) {
        api.comOutroUsuario(() -> api.get(rota));
    }

    @Entao("a lista deve estar vazia")
    public void lista_vazia() {
        assertEquals(200, api.status());
        assertTrue(api.corpo().isArray(), "Era esperada uma lista: " + api.corpo());
        assertEquals(0, api.corpo().size());
    }

    @Entao("o resumo dele deve estar zerado")
    public void resumo_zerado() {
        assertEquals(200, api.status());
        assertEquals(0, api.corpo().path("totalReceitas").decimalValue().compareTo(BigDecimal.ZERO));
        assertEquals(0, api.corpo().path("totalDespesas").decimalValue().compareTo(BigDecimal.ZERO));
        assertEquals(0, api.corpo().path("saldo").decimalValue().compareTo(BigDecimal.ZERO));
    }

    @Quando("outro usuário exporta o CSV do mês atual")
    public void outro_usuario_exporta() {
        api.comOutroUsuario(() -> api.getCsv("/api/transacoes/exportar"));
    }

    @Entao("o CSV dele deve ter apenas o cabeçalho")
    public void csv_so_cabecalho() {
        assertEquals(200, api.status());
        assertEquals(1, api.texto().trim().split("\r\n").length, "CSV de outro usuário: " + api.texto());
    }

    @Dado("que eu mudei meu orçamento para {int}\\/{int}\\/{int}")
    public void mudar_orcamento(int necessidades, int lazer, int investimentos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("NECESSIDADES", necessidades);
        corpo.put("LAZER", lazer);
        corpo.put("INVESTIMENTOS", investimentos);
        api.put("/api/orcamento", corpo);
        assertEquals(200, api.status(), "Orçamento não mudou: " + api.corpo());
    }

    @Entao("o orçamento dele deve continuar {int}\\/{int}\\/{int}")
    public void orcamento_intacto(int necessidades, int lazer, int investimentos) {
        assertEquals(200, api.status());
        assertEquals(necessidades, percentual("NECESSIDADES"));
        assertEquals(lazer, percentual("LAZER"));
        assertEquals(investimentos, percentual("INVESTIMENTOS"));
    }

    private int percentual(String grupo) {
        return StreamSupport.stream(api.corpo().spliterator(), false)
                .filter(o -> o.path("grupo").asText().equals(grupo))
                .findFirst()
                .orElseThrow()
                .path("percentual").decimalValue().intValue();
    }
}
