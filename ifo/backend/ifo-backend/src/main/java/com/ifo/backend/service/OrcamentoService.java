package com.ifo.backend.service;

import com.ifo.backend.model.OrcamentoMensal;
import com.ifo.backend.repository.OrcamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrcamentoService {

    @Autowired
    private OrcamentoRepository repository;

    public List<OrcamentoMensal> buscarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public OrcamentoMensal salvar(OrcamentoMensal orcamento) {
        return repository.save(orcamento);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Orçamento não encontrado");
        }
        repository.deleteById(id);
    }
}