package com.ifo.backend.steps;

import io.cucumber.java.pt.Dado;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Passos comuns a todas as features: quem está autenticado e o que a API respondeu
public class SessaoSteps {

    @Autowired
    private ApiCliente api;

    @Value("${ifo.jwt.secret}")
    private String segredoJwt;

    private SecretKey chaveDoServidor() {
        return Keys.hmacShaKeyFor(segredoJwt.getBytes(StandardCharsets.UTF_8));
    }

    private String idDeUmUsuarioReal() {
        api.cadastrarUsuario(BigDecimal.valueOf(1500));
        return String.valueOf(api.corpo().path("usuario").path("idUsuario").asLong());
    }

    private void autenticarComToken(String token) {
        api.autenticarCom(token);
    }

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

    // Os tokens abaixo são forjados para um usuário que EXISTE: o que barra a requisição é o token, não o id
    @Dado("que estou com um token expirado")
    public void token_expirado() {
        Date ontem = Date.from(Instant.now().minus(Duration.ofDays(1)));
        autenticarComToken(Jwts.builder().subject(idDeUmUsuarioReal()).expiration(ontem).signWith(chaveDoServidor()).compact());
    }

    @Dado("que estou com um token sem assinatura")
    public void token_sem_assinatura() {
        autenticarComToken(Jwts.builder().subject(idDeUmUsuarioReal()).compact());
    }

    @Dado("que estou com um token assinado com outra chave")
    public void token_outra_chave() {
        SecretKey outra = Keys.hmacShaKeyFor("outra-chave-qualquer-com-mais-de-32-caracteres".getBytes(StandardCharsets.UTF_8));
        autenticarComToken(Jwts.builder().subject(idDeUmUsuarioReal()).signWith(outra).compact());
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

    // Dados de outro usuário nunca vêm no mesmo formato que os meus: aqui só se confere que nada de senha/hash vaza
    @E("a resposta não deve expor senha nem hash")
    public void sem_senha() {
        String json = api.corpo().toString().toLowerCase();
        assertFalse(json.contains("senha"), "A resposta cita senha: " + json);
        assertFalse(json.contains("hash"), "A resposta cita hash: " + json);
        assertFalse(json.contains("$2a$"), "A resposta contém um hash BCrypt: " + json);
    }

    @E("o erro deve apontar o campo {string}")
    public void campo_com_erro(String campo) {
        assertTrue(api.corpo().path("campos").has(campo),
                "Campo '" + campo + "' não aparece nos erros: " + api.corpo().path("campos"));
    }
}
