package com.ifo.backend.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Login pela API de verdade (BCrypt + JWT + filtro de segurança), nada de senha fixa
public class LoginSteps {

    @Autowired
    private ApiCliente api;

    private String email;

    @Dado("que existe um usuário cadastrado com a senha {string}")
    public void usuario_cadastrado(String senha) {
        email = "login-" + UUID.randomUUID() + "@ifo.teste";
        api.post("/api/auth/cadastro", Map.of("nome", "Usuário Teste", "email", email, "senha", senha, "rendaMensal", 1500));
        assertEquals(201, api.status(), "Cadastro falhou: " + api.corpo());
    }

    @Quando("ele faz login com a senha {string}")
    public void fazer_login(String senha) {
        api.post("/api/auth/login", Map.of("email", email, "senha", senha));
    }

    @Quando("alguém tenta login com email {string} e senha {string}")
    public void tentar_login(String emailInformado, String senha) {
        api.post("/api/auth/login", Map.of("email", emailInformado, "senha", senha));
    }

    @Entao("o login deve ser realizado com sucesso")
    public void login_sucesso() {
        assertEquals(200, api.status(), "Login falhou: " + api.corpo());
        assertEquals(email, api.corpo().path("usuario").path("email").asText());
    }

    // Prova de que o token funciona: usa ele numa rota protegida
    @Entao("ele deve receber um token de acesso válido")
    public void token_valido() {
        String token = api.corpo().path("token").asText();
        assertFalse(token.isBlank(), "Resposta sem token");
        api.autenticarCom(token);
        api.get("/api/usuarios/me");
        assertEquals(200, api.status());
        assertEquals(email, api.corpo().path("email").asText());
    }

    @Entao("o sistema deve retornar erro de autenticação")
    public void erro_autenticacao() {
        assertEquals(401, api.status());
        assertTrue(api.corpo().path("token").isMissingNode(), "Erro de login não pode devolver token");
    }

    @Entao("a mensagem deve ser {string}")
    public void mensagem_erro(String mensagem) {
        assertEquals(mensagem, api.corpo().path("mensagem").asText());
    }
}
