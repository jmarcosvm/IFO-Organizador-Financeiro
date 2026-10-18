package com.ifo.backend.security;

import com.ifo.backend.model.Usuario;
import org.junit.jupiter.api.Test;

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

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setEmail("teste@ifo.teste");
        return usuario;
    }
}
