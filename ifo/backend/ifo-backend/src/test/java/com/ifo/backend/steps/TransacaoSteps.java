package com.ifo.backend.steps;

import com.fasterxml.jackson.databind.JsonNode;
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

// Todos os passos chamam /api/transacoes, /api/resumo etc. por HTTP, com JWT de verdade
public class TransacaoSteps {

    @Autowired
    private ApiCliente api;

    private long idTransacao;
    private JsonNode lista;
    private JsonNode resumo;
    private long tempoDecorrido;

    @Quando("eu lanço {bigdecimal} na categoria {string}")
    @Dado("que eu lancei {bigdecimal} na categoria {string}")
    public void lancar(BigDecimal valor, String nomeCategoria) {
        api.post("/api/transacoes", corpo(valor, nomeCategoria, LocalDate.now()));
        if (api.status() == 201) {
            idTransacao = api.corpo().path("transacao").path("idTransacao").asLong();
        }
    }

    @Dado("que eu lancei {bigdecimal} na categoria {string} em {string}")
    public void lancar_em(BigDecimal valor, String nomeCategoria, String data) {
        api.post("/api/transacoes", corpo(valor, nomeCategoria, LocalDate.parse(data)));
        assertEquals(201, api.status(), "Lançamento falhou: " + api.corpo());
    }

    @Quando("eu listo as transações de {int}\\/{int}")
    public void listar_mes(int mes, int ano) {
        api.get("/api/transacoes?mes=" + mes + "&ano=" + ano);
        lista = api.corpo();
    }

    @Entao("todas devem ser de {int}\\/{int}")
    public void todas_do_mes(int mes, int ano) {
        assertTrue(StreamSupport.stream(lista.spliterator(), false)
                .map(t -> LocalDate.parse(t.path("data").asText()))
                .allMatch(d -> d.getMonthValue() == mes && d.getYear() == ano));
    }

    @Entao("a transação deve ser salva com sucesso")
    public void transacao_salva() {
        assertEquals(201, api.status(), "Corpo da resposta: " + api.corpo());
        JsonNode transacao = api.corpo().path("transacao");
        assertTrue(transacao.path("idTransacao").asLong() > 0, "ID não foi gerado");
        assertTrue(transacao.path("valor").decimalValue().compareTo(BigDecimal.ZERO) > 0, "Valor deve ser positivo");
    }

    @Entao("o tipo derivado da categoria deve ser {string} sem grupo")
    public void tipo_sem_grupo(String tipo) {
        JsonNode categoria = api.corpo().path("transacao").path("categoria");
        assertEquals(tipo, categoria.path("tipo").asText());
        assertTrue(categoria.path("grupo").isNull());
    }

    @Entao("o tipo derivado da categoria deve ser {string} no grupo {string}")
    public void tipo_com_grupo(String tipo, String grupo) {
        JsonNode categoria = api.corpo().path("transacao").path("categoria");
        assertEquals(tipo, categoria.path("tipo").asText());
        assertEquals(grupo, categoria.path("grupo").asText());
    }

    @Quando("outro usuário tenta excluir essa transação")
    public void outro_usuario_exclui() {
        api.comOutroUsuario(() -> api.delete("/api/transacoes/" + idTransacao));
    }

    @Entao("a exclusão deve ser negada com a mensagem {string}")
    public void exclusao_negada(String mensagem) {
        assertEquals(404, api.status());
        assertEquals(mensagem, api.corpo().path("mensagem").asText());
        api.get("/api/transacoes");
        assertEquals(1, api.corpo().size(), "A transação não deveria ter sido apagada");
    }

    @Quando("eu edito essa transação para {bigdecimal} na categoria {string}")
    public void editar(BigDecimal valor, String nomeCategoria) {
        api.put("/api/transacoes/" + idTransacao, corpo(valor, nomeCategoria, LocalDate.now()));
    }

    @Entao("a transação deve manter o mesmo id")
    public void mesmo_id() {
        assertEquals(200, api.status(), "Corpo da resposta: " + api.corpo());
        assertEquals(idTransacao, api.corpo().path("transacao").path("idTransacao").asLong());
    }

    @Quando("outro usuário tenta editar essa transação para {bigdecimal} na categoria {string}")
    public void outro_usuario_edita(BigDecimal valor, String nomeCategoria) {
        Map<String, Object> corpo = corpo(valor, nomeCategoria, LocalDate.now());
        api.comOutroUsuario(() -> api.put("/api/transacoes/" + idTransacao, corpo));
    }

    @Entao("a edição deve ser negada com a mensagem {string}")
    public void edicao_negada(String mensagem) {
        assertEquals(404, api.status());
        assertEquals(mensagem, api.corpo().path("mensagem").asText());
    }

    // Confere no banco (relendo pela listagem), não só na resposta do PUT/POST
    @Entao("ela deve estar com valor {bigdecimal} na categoria {string} no grupo {string}")
    public void conferir_no_banco(BigDecimal valor, String nomeCategoria, String grupo) {
        api.get("/api/transacoes");
        JsonNode salva = StreamSupport.stream(api.corpo().spliterator(), false)
                .filter(t -> t.path("idTransacao").asLong() == idTransacao)
                .findFirst()
                .orElseThrow();
        assertEquals(0, salva.path("valor").decimalValue().compareTo(valor));
        assertEquals(nomeCategoria, salva.path("categoria").path("nome").asText());
        assertEquals(grupo, salva.path("categoria").path("grupo").asText());
    }

    @Quando("eu peço o resumo de {int}\\/{int}")
    public void pedir_resumo(int mes, int ano) {
        api.get("/api/resumo?mes=" + mes + "&ano=" + ano);
        resumo = api.corpo();
    }

    @Entao("o resumo deve ter receitas {bigdecimal}; despesas {bigdecimal}; saldo {bigdecimal}")
    public void conferir_resumo(BigDecimal receitas, BigDecimal despesas, BigDecimal saldo) {
        assertEquals(0, resumo.path("totalReceitas").decimalValue().compareTo(receitas));
        assertEquals(0, resumo.path("totalDespesas").decimalValue().compareTo(despesas));
        assertEquals(0, resumo.path("saldo").decimalValue().compareTo(saldo));
    }

    @Entao("o grupo {string} deve somar {bigdecimal} e representar {bigdecimal}% das despesas")
    public void conferir_grupo(String grupo, BigDecimal total, BigDecimal percentual) {
        JsonNode doGrupo = StreamSupport.stream(resumo.path("despesasPorGrupo").spliterator(), false)
                .filter(g -> g.path("grupo").asText().equals(grupo))
                .findFirst()
                .orElseThrow();
        assertEquals(0, doGrupo.path("total").decimalValue().compareTo(total));
        assertEquals(0, doGrupo.path("percentual").decimalValue().compareTo(percentual));
    }

    @Dado("que eu tenho {int} transações lançadas")
    public void tenho_transacoes(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            api.post("/api/transacoes", corpo(BigDecimal.valueOf(100), "Alimentação", LocalDate.now()));
            assertEquals(201, api.status());
        }
    }

    @Quando("eu solicito a lista de transações")
    public void solicitar_lista() {
        long inicio = System.currentTimeMillis();
        api.get("/api/transacoes");
        tempoDecorrido = System.currentTimeMillis() - inicio;
        lista = api.corpo();
    }

    @Entao("devo receber {int} transações")
    public void quantidade_recebida(int quantidade) {
        assertEquals(quantidade, lista.size());
    }

    @Entao("o tempo de resposta deve ser menor que {int} segundos")
    public void tempo_resposta(int segundos) {
        assertTrue(tempoDecorrido < segundos * 1000L,
                "Tempo excedido: " + tempoDecorrido + "ms (máximo: " + (segundos * 1000) + "ms)");
    }

    private Map<String, Object> corpo(BigDecimal valor, String nomeCategoria, LocalDate data) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("idCategoria", api.idCategoria(nomeCategoria));
        corpo.put("valor", valor);
        corpo.put("data", data.toString());
        corpo.put("descricao", "Transação de teste Cucumber");
        return corpo;
    }
}
