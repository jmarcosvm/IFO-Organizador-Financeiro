package com.ifo.backend.service;

import com.ifo.backend.dto.MetaRequest;
import com.ifo.backend.dto.MetaResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.model.MetaFinanceira;
import com.ifo.backend.repository.MetaFinanceiraRepository;
import com.ifo.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MetaFinanceiraService {

    private final MetaFinanceiraRepository repository;
    private final UsuarioRepository usuarioRepository;

    public MetaFinanceiraService(MetaFinanceiraRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<MetaResponse> listar(Long idUsuario) {
        return repository.findByUsuarioIdUsuarioOrderByIdMetaAsc(idUsuario).stream()
                .map(MetaResponse::from)
                .toList();
    }

    @Transactional
    public MetaResponse criar(Long idUsuario, MetaRequest request) {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        meta.setTituloMeta(request.tituloMeta().trim());
        meta.setValorObjetivo(request.valorObjetivo());
        meta.setPrazo(request.prazo());
        return MetaResponse.from(repository.save(meta));
    }

    @Transactional
    public void excluir(Long idUsuario, Long idMeta) {
        MetaFinanceira meta = repository.findByIdMetaAndUsuarioIdUsuario(idMeta, idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Meta não encontrada."));
        repository.delete(meta);
    }
}
