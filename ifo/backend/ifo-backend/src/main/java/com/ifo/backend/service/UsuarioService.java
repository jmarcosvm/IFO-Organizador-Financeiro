package com.ifo.backend.service;

import com.ifo.backend.dto.AtualizarUsuarioRequest;
import com.ifo.backend.dto.UsuarioResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.model.Usuario;
import com.ifo.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long idUsuario) {
        return UsuarioResponse.from(buscarEntidade(idUsuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long idUsuario, AtualizarUsuarioRequest request) {
        Usuario usuario = buscarEntidade(idUsuario);
        usuario.setNome(request.nome().trim());
        usuario.setRendaMensal(request.rendaMensal());
        return UsuarioResponse.from(usuario);
    }

    private Usuario buscarEntidade(Long idUsuario) {
        return repository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }
}
