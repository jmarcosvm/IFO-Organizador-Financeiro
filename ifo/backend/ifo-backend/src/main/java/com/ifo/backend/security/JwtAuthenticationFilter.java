package com.ifo.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

// Lê "Authorization: Bearer <token>" e autentica a requisição com o id do usuário.
// Nos controllers, o id chega por @AuthenticationPrincipal Long idUsuario.
// Não é @Component de propósito: só deve rodar dentro da cadeia do Spring Security.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIXO)) {
            jwtService.extrairIdUsuario(header.substring(PREFIXO.length()))
                    .ifPresent(idUsuario -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(idUsuario, null, List.of())));
        }
        chain.doFilter(request, response);
    }
}
