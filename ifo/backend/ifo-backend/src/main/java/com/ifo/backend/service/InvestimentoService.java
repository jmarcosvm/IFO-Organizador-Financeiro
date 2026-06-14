package com.ifo.backend.service;

import com.ifo.backend.model.Investimento;
import com.ifo.backend.repository.InvestimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InvestimentoService {

    @Autowired
    private InvestimentoRepository repository;

    public List<Investimento> buscarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public Investimento salvar(Investimento investimento) {
        return repository.save(investimento);
    }
}