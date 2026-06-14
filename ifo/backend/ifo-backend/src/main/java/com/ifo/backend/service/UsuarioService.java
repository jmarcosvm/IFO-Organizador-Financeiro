package com.ifo.backend.service;

import com.ifo.backend.model.Usuario;
import com.ifo.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    public Usuario salvar(Usuario usuario) {
        return repository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }

    public Usuario login(String email, String senha) {
        Optional<Usuario> usuario = repository.findByEmail(email);
        if (usuario.isPresent() && usuario.get().getSenha().equals(senha)) {
            return usuario.get();
        }
        return null;
    }
}