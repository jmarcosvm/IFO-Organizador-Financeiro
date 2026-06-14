package com.ifo.backend.service;

import com.ifo.backend.model.MetaFinanceira;
import com.ifo.backend.repository.MetaFinanceiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class MetaFinanceiraService {

    @Autowired
    private MetaFinanceiraRepository repository;

    public List<MetaFinanceira> buscarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public MetaFinanceira salvar(MetaFinanceira meta) {
        return repository.save(meta);
    }

    public MetaFinanceira atualizarValor(Long id, BigDecimal novoValor) {
        MetaFinanceira meta = repository.findById(id).orElseThrow();
        meta.setValorAtual(novoValor);
        return repository.save(meta);
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Meta não encontrada");
        }
        repository.deleteById(id);
    }
}