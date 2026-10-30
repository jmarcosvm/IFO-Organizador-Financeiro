package com.ifo.backend.steps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

// "Navegador" dos cenários: manda requisições HTTP de verdade (MockMvc) para a API e guarda o
// token do usuário atual e a última resposta. Um por cenário (@ScenarioScope), então nada vaza
// de um cenário para o outro.
@Component
@ScenarioScope
public class ApiCliente {

    private final MockMvc mockMvc;
    private final ObjectMapper json;

    private String token;
    private MvcResult ultimaResposta;
    private JsonNode ultimoCorpo;
    private Map<String, Short> categorias;

    @Autowired
    public ApiCliente(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        // Números decimais lidos como BigDecimal: dinheiro nunca passa por double
        this.json = objectMapper.copy().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

    // ---- usuários ----

    // Cadastra um usuário novo pela API e devolve o token dele (não troca o usuário atual)
    public String cadastrarUsuario(BigDecimal rendaMensal) {
        enviar(HttpMethod.POST, "/api/auth/cadastro", Map.of(
                "nome", "Usuário Teste",
                "email", "cenario-" + UUID.randomUUID() + "@ifo.teste",
                "senha", "senhaSegura123",
                "rendaMensal", rendaMensal));
        return corpo().path("token").asText();
    }

    public void autenticarCom(String novoToken) {
        this.token = novoToken;
    }

    // Executa a ação como se fosse outro usuário (recém-cadastrado) e volta para o usuário atual;
    // a última resposta fica guardada para o cenário conferir
    public void comOutroUsuario(Runnable acao) {
        String dono = token;
        token = cadastrarUsuario(BigDecimal.valueOf(1500));
        try {
            acao.run();
        } finally {
            token = dono;
        }
    }

    public void semToken() {
        this.token = null;
    }

    public String token() {
        return token;
    }

    // ---- requisições (usam o token atual, se houver) ----

    public void get(String uri) {
        enviar(HttpMethod.GET, uri, null);
    }

    public void post(String uri, Object corpo) {
        enviar(HttpMethod.POST, uri, corpo);
    }

    public void put(String uri, Object corpo) {
        enviar(HttpMethod.PUT, uri, corpo);
    }

    public void delete(String uri) {
        enviar(HttpMethod.DELETE, uri, null);
    }

    public void enviar(HttpMethod metodo, String uri, Object corpo) {
        try {
            MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders.request(metodo, uri)
                    .accept(MediaType.APPLICATION_JSON);
            if (token != null) {
                requisicao.header("Authorization", "Bearer " + token);
            }
            if (corpo != null) {
                requisicao.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo));
            }
            ultimaResposta = mockMvc.perform(requisicao).andReturn();
            String texto = ultimaResposta.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ultimoCorpo = texto.isBlank() ? json.nullNode() : json.readTree(texto);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao chamar " + metodo + " " + uri, e);
        }
    }

    // ---- resposta ----

    public int status() {
        return ultimaResposta.getResponse().getStatus();
    }

    public JsonNode corpo() {
        return ultimoCorpo;
    }

    // ---- ajudantes ----

    // Categorias são fixas (ADR-001): busca o id pelo nome uma vez por cenário
    public Short idCategoria(String nome) {
        if (categorias == null) {
            get("/api/categorias");
            categorias = new LinkedHashMap<>();
            corpo().forEach(c -> categorias.put(c.path("nome").asText(), (short) c.path("idCategoria").asInt()));
        }
        Short id = categorias.get(nome);
        if (id == null) {
            throw new IllegalArgumentException("Categoria inexistente: " + nome);
        }
        return id;
    }
}
