package com.ifo.backend.service;

import com.ifo.backend.model.Categoria;
import com.ifo.backend.model.TipoCategoria;
import com.ifo.backend.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository repository;

    public List<Categoria> buscarTodas() {
        return repository.findAll();
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Categoria salvar(Categoria categoria) {
        return repository.save(categoria);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Categoria não encontrada");
        }
        repository.deleteById(id);
    }

    public void inicializarCategoriasPadrao() {
        for (TipoCategoria tipo : TipoCategoria.values()) {
            Optional<Categoria> existente = repository.findAll().stream()
                    .filter(c -> c.getTipo() == tipo)
                    .findFirst();

            if (existente.isEmpty()) {
                Categoria categoria = new Categoria();
                categoria.setTipo(tipo);
                categoria.setDescricao(tipo.getDescricao());
                salvar(categoria);
            }
        }
    }
}