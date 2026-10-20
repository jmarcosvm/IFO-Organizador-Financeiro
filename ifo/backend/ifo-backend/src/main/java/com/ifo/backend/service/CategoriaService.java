package com.ifo.backend.service;

import com.ifo.backend.dto.CategoriaResponse;
import com.ifo.backend.exception.RegraNegocioException;
import com.ifo.backend.model.Categoria;
import com.ifo.backend.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAllByOrderByIdCategoriaAsc().stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    // Categoria inválida é erro do cliente (400), não "recurso não encontrado"
    @Transactional(readOnly = true)
    public Categoria buscarEntidade(Short idCategoria) {
        return repository.findById(idCategoria)
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada."));
    }
}
