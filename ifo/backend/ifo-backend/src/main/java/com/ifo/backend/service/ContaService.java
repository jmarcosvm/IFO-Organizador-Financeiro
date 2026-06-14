package com.ifo.backend.service;

import com.ifo.backend.model.ContaCarteira;
import com.ifo.backend.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContaService {

    @Autowired
    private ContaRepository repository;

    public List<ContaCarteira> buscarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public ContaCarteira salvar(ContaCarteira conta) {
        return repository.save(conta);
    }
}