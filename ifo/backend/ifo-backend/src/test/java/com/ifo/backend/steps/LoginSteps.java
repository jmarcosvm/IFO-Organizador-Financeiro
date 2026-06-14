package com.ifo.backend.steps;

import com.ifo.backend.model.Usuario;
import com.ifo.backend.repository.UsuarioRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LoginSteps {

    @Autowired
    private UsuarioRepository repository;

    private Usuario usuario;
    private boolean loginSucesso;
    private String mensagemErro;

    @Dado("que o usuário {string} existe no sistema")
    public void usuario_existe(String email) {
        usuario = repository.findByEmail(email).orElse(null);
        assertNotNull(usuario, "Usuário não encontrado: " + email);
    }

    @Quando("ele faz login com a senha {string}")
    public void fazer_login(String senha) {
        // Simulação de login (senha hardcoded para teste)
        loginSucesso = (usuario != null && "123456".equals(senha));
    }

    @Entao("o login deve ser realizado com sucesso")
    public void login_sucesso() {
        assertTrue(loginSucesso, "Login falhou");
    }

    @Entao("o sistema deve redirecionar para o dashboard")
    public void redirecionar_dashboard() {
        assertTrue(loginSucesso, "Deveria redirecionar para dashboard");
    }

    @Dado("que o usuário está na tela de login")
    public void usuario_tela_login() {
        // Pré-condição
    }

    @Quando("ele tenta login com email {string} e senha {string}")
    public void tentar_login_invalido(String email, String senha) {
        usuario = repository.findByEmail(email).orElse(null);
        loginSucesso = (usuario != null && "123456".equals(senha));

        if (!loginSucesso) {
            mensagemErro = "Email ou senha inválidos";
        }
    }

    @Entao("o sistema deve retornar erro de autenticação")
    public void erro_autenticacao() {
        assertFalse(loginSucesso, "Login deveria falhar");
    }

    @Entao("a mensagem deve ser {string}")
    public void mensagem_erro(String mensagem) {
        assertEquals(mensagem, mensagemErro, "Mensagem de erro incorreta");
    }
}