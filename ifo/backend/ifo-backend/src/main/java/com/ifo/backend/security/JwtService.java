package com.ifo.backend.security;

import com.ifo.backend.model.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

// Gera e valida o token JWT (HS256). O "subject" é o id do usuário.
@Service
public class JwtService {

    private static final int TAMANHO_MINIMO_CHAVE = 32;
    private static final String PREFIXO_SEGREDO_DE_EXEMPLO = "troque";

    private final SecretKey chave;
    private final Duration expiracao;

    public JwtService(@Value("${ifo.jwt.secret}") String segredo,
                      @Value("${ifo.jwt.expiracao-minutos}") long expiracaoMinutos) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_CHAVE) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos " + TAMANHO_MINIMO_CHAVE + " caracteres.");
        }
        // O .env.example tem 53 caracteres e passaria na checagem de tamanho: sem isto, quem copiasse o
        // exemplo sem trocar a chave rodaria com um segredo público no GitHub
        if (segredo.toLowerCase(Locale.ROOT).startsWith(PREFIXO_SEGREDO_DE_EXEMPLO)) {
            throw new IllegalStateException("JWT_SECRET ainda é o valor de exemplo do .env.example. Gere uma chave aleatória.");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.expiracao = Duration.ofMinutes(expiracaoMinutos);
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getIdUsuario().toString())
                .claim("email", usuario.getEmail())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracao)))
                .signWith(chave)
                .compact();
    }

    // Vazio se o token estiver ausente, adulterado ou expirado
    public Optional<Long> extrairIdUsuario(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.of(Long.valueOf(subject));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
