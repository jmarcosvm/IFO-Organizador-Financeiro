package com.ifo.backend.service;

import com.ifo.backend.dto.AuthResponse;
import com.ifo.backend.dto.CadastroRequest;
import com.ifo.backend.dto.LoginRequest;
import com.ifo.backend.exception.CredenciaisInvalidasException;
import com.ifo.backend.exception.EmailJaCadastradoException;
import com.ifo.backend.model.Usuario;
import com.ifo.backend.repository.UsuarioRepository;
import com.ifo.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UsuarioRepository repository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder(4); // custo baixo só para o teste ficar rápido
        JwtService jwtService = new JwtService("chave-de-teste-com-mais-de-32-caracteres-123", 60);
        authService = new AuthService(repository, passwordEncoder, jwtService);

        when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> {
            Usuario usuario = invocacao.getArgument(0);
            usuario.setIdUsuario(1L);
            return usuario;
        });
    }

    @Test
    void cadastroGravaHashBCryptENormalizaEmail() {
        AuthResponse resposta = authService.cadastrar(
                new CadastroRequest("João", "  Joao@IFO.Teste ", "senhaSegura123", null));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(salvo.capture());
        assertEquals("joao@ifo.teste", salvo.getValue().getEmail());
        assertNotEquals("senhaSegura123", salvo.getValue().getSenhaHash());
        assertTrue(passwordEncoder.matches("senhaSegura123", salvo.getValue().getSenhaHash()));
        assertEquals(BigDecimal.ZERO, salvo.getValue().getRendaMensal());
        assertNotNull(resposta.token());
    }

    @Test
    void cadastroComEmailExistenteFalha() {
        when(repository.existsByEmail("joao@ifo.teste")).thenReturn(true);

        assertThrows(EmailJaCadastradoException.class, () -> authService.cadastrar(
                new CadastroRequest("João", "joao@ifo.teste", "senhaSegura123", BigDecimal.TEN)));
        verify(repository, never()).save(any());
    }

    @Test
    void loginComSenhaCorretaDevolveToken() {
        when(repository.findByEmail("joao@ifo.teste")).thenReturn(Optional.of(usuarioComSenha("senhaSegura123")));

        AuthResponse resposta = authService.login(new LoginRequest("JOAO@ifo.teste", "senhaSegura123"));

        assertEquals("joao@ifo.teste", resposta.usuario().email());
        assertNotNull(resposta.token());
    }

    @Test
    void loginComSenhaErradaFalha() {
        when(repository.findByEmail("joao@ifo.teste")).thenReturn(Optional.of(usuarioComSenha("senhaSegura123")));

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login(new LoginRequest("joao@ifo.teste", "senhaErrada")));
    }

    @Test
    void loginComEmailInexistenteFalhaComAMesmaMensagem() {
        when(repository.findByEmail(any())).thenReturn(Optional.empty());

        CredenciaisInvalidasException erro = assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login(new LoginRequest("ninguem@ifo.teste", "qualquer123")));
        assertEquals("E-mail ou senha inválidos.", erro.getMessage());
    }

    private Usuario usuarioComSenha(String senha) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNome("João");
        usuario.setEmail("joao@ifo.teste");
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        return usuario;
    }
}
