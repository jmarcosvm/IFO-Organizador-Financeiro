package com.ifo.backend.steps;

import com.ifo.backend.dto.AuthResponse;
import com.ifo.backend.dto.CadastroRequest;
import com.ifo.backend.dto.LoginRequest;
import com.ifo.backend.exception.CredenciaisInvalidasException;
import com.ifo.backend.security.JwtService;
import com.ifo.backend.service.AuthService;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Chama o AuthService de verdade (BCrypt + JWT), nada de senha fixa
public class LoginSteps {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    private String email;
    private AuthResponse resposta;
    private String mensagemErro;

    @Dado("que existe um usuário cadastrado com a senha {string}")
    public void usuario_cadastrado(String senha) {
        email = "login-" + UUID.randomUUID() + "@ifo.teste";
        authService.cadastrar(new CadastroRequest("Usuário Teste", email, senha, BigDecimal.valueOf(1500)));
    }

    @Quando("ele faz login com a senha {string}")
    public void fazer_login(String senha) {
        tentarLogin(email, senha);
    }

    @Quando("alguém tenta login com email {string} e senha {string}")
    public void tentar_login(String emailInformado, String senha) {
        tentarLogin(emailInformado, senha);
    }

    @Entao("o login deve ser realizado com sucesso")
    public void login_sucesso() {
        assertNull(mensagemErro, "Login falhou: " + mensagemErro);
        assertEquals(email, resposta.usuario().email());
    }

    @Entao("ele deve receber um token de acesso válido")
    public void token_valido() {
        assertEquals(resposta.usuario().idUsuario(), jwtService.extrairIdUsuario(resposta.token()).orElseThrow());
    }

    @Entao("o sistema deve retornar erro de autenticação")
    public void erro_autenticacao() {
        assertNull(resposta, "Login deveria falhar");
        assertNotNull(mensagemErro);
    }

    @Entao("a mensagem deve ser {string}")
    public void mensagem_erro(String mensagem) {
        assertEquals(mensagem, mensagemErro);
    }

    private void tentarLogin(String emailInformado, String senha) {
        try {
            resposta = authService.login(new LoginRequest(emailInformado, senha));
        } catch (CredenciaisInvalidasException e) {
            mensagemErro = e.getMessage();
        }
    }
}
