package com.ifo.backend.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Passos comuns a todas as features: quem está autenticado e o que a API respondeu
public class SessaoSteps {

    @Autowired
    private ApiCliente api;

    @Dado("que estou autenticado como um novo usuário")
    public void novo_usuario() {
        novo_usuario_com_renda(BigDecimal.valueOf(1500));
    }

    @Dado("que estou autenticado como um novo usuário com renda de {bigdecimal}")
    public void novo_usuario_com_renda(BigDecimal renda) {
        api.autenticarCom(api.cadastrarUsuario(renda));
    }

    @Dado("que não estou autenticado")
    public void sem_autenticacao() {
        api.semToken();
    }

    @Dado("que estou com um token adulterado")
    public void token_adulterado() {
        api.autenticarCom(api.cadastrarUsuario(BigDecimal.valueOf(1500)) + "adulterado");
    }

    @Quando("eu consulto {string}")
    public void consultar(String rota) {
        api.get(rota);
    }

    @Entao("a resposta deve ter status {int}")
    public void status(int esperado) {
        assertEquals(esperado, api.status(), "Corpo da resposta: " + api.corpo());
    }

    @E("a mensagem de erro deve ser {string}")
    public void mensagem_de_erro(String mensagem) {
        assertEquals(mensagem, api.corpo().path("mensagem").asText());
    }

    @E("o erro deve apontar o campo {string}")
    public void campo_com_erro(String campo) {
        assertTrue(api.corpo().path("campos").has(campo),
                "Campo '" + campo + "' não aparece nos erros: " + api.corpo().path("campos"));
    }
}
