package com.ifo.backend.service;

import com.ifo.backend.dto.AuthResponse;
import com.ifo.backend.dto.CadastroRequest;
import com.ifo.backend.dto.LoginRequest;
import com.ifo.backend.dto.UsuarioResponse;
import com.ifo.backend.exception.CredenciaisInvalidasException;
import com.ifo.backend.exception.EmailJaCadastradoException;
import com.ifo.backend.model.Usuario;
import com.ifo.backend.repository.UsuarioRepository;
import com.ifo.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Locale;

@Service
public class AuthService {

    private static final String MENSAGEM_LOGIN_INVALIDO = "E-mail ou senha inválidos.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OrcamentoService orcamentoService;
    // Hash de uma senha qualquer, só para o login gastar o mesmo tempo de BCrypt com e-mail inexistente
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       OrcamentoService orcamentoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.orcamentoService = orcamentoService;
        this.hashFicticio = passwordEncoder.encode("senha-ficticia-so-para-gastar-tempo");
    }

    @Transactional
    public AuthResponse cadastrar(CadastroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException("Este e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setRendaMensal(request.rendaMensal() != null ? request.rendaMensal() : BigDecimal.ZERO);
        usuario = usuarioRepository.save(usuario);
        orcamentoService.criarPadrao(usuario);

        return new AuthResponse(jwtService.gerarToken(usuario), UsuarioResponse.from(usuario));
    }

    // Mesma mensagem e mesmo tempo (BCrypt sempre roda) para e-mail inexistente e senha errada:
    // nem a resposta nem a demora revelam quais e-mails existem
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(request.email())).orElse(null);
        boolean senhaCorreta = passwordEncoder.matches(request.senha(),
                usuario != null ? usuario.getSenhaHash() : hashFicticio);
        if (usuario == null || !senhaCorreta) {
            throw new CredenciaisInvalidasException(MENSAGEM_LOGIN_INVALIDO);
        }

        return new AuthResponse(jwtService.gerarToken(usuario), UsuarioResponse.from(usuario));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
