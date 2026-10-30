package com.ifo.backend.security;

import com.ifo.backend.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SEGREDO = "chave-de-teste-com-mais-de-32-caracteres-123";

    @Test
    void gerarTokenEExtrairIdDoUsuario() {
        JwtService jwtService = new JwtService(SEGREDO, 60);

        String token = jwtService.gerarToken(usuario(42L));

        assertEquals(42L, jwtService.extrairIdUsuario(token).orElseThrow());
    }

    @Test
    void tokenAssinadoComOutraChaveEhRejeitado() {
        String token = new JwtService("outra-chave-de-teste-com-mais-de-32-caracteres", 60).gerarToken(usuario(1L));

        assertTrue(new JwtService(SEGREDO, 60).extrairIdUsuario(token).isEmpty());
    }

    @Test
    void tokenExpiradoEhRejeitado() {
        JwtService jwtService = new JwtService(SEGREDO, -1);

        assertTrue(jwtService.extrairIdUsuario(jwtService.gerarToken(usuario(1L))).isEmpty());
    }

    @Test
    void tokenMalFormadoEhRejeitado() {
        assertTrue(new JwtService(SEGREDO, 60).extrairIdUsuario("isto-nao-e-um-jwt").isEmpty());
    }

    @Test
    void chaveCurtaImpedeAInicializacao() {
        assertThrows(IllegalStateException.class, () -> new JwtService("curta", 60));
    }

    // O .env.example tem mais de 32 caracteres: só o tamanho não basta para barrar o segredo de exemplo
    @Test
    void segredoDeExemploDoEnvExampleImpedeAInicializacao() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("troque-por-uma-chave-aleatoria-com-32-ou-mais-caracteres", 60));
        assertThrows(IllegalStateException.class,
                () -> new JwtService("TROQUE-por-uma-chave-aleatoria-com-32-ou-mais-caracteres", 60));
    }

    // Ataque clássico "alg: none": token com o id certo, mas sem assinatura
    @Test
    void tokenSemAssinaturaEhRejeitado() {
        String semAssinatura = Jwts.builder().subject("42").compact();

        assertTrue(new JwtService(SEGREDO, 60).extrairIdUsuario(semAssinatura).isEmpty());
    }

    @Test
    void tokenComSubjectQueNaoEhNumeroEhRejeitado() {
        SecretKey chave = Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject("nao-e-um-id").signWith(chave).compact();

        assertTrue(new JwtService(SEGREDO, 60).extrairIdUsuario(token).isEmpty());
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setEmail("teste@ifo.teste");
        return usuario;
    }
}
