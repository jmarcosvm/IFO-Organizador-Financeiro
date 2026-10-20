package com.ifo.backend.service;

import com.ifo.backend.dto.OrcamentoResponse;
import com.ifo.backend.model.Orcamento;
import com.ifo.backend.repository.OrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
public class OrcamentoService {

    private final OrcamentoRepository repository;

    public OrcamentoService(OrcamentoRepository repository) {
        this.repository = repository;
    }

    // Ordem fixa: Necessidades, Lazer, Investimentos
    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listar(Long idUsuario) {
        return repository.findByUsuarioIdUsuario(idUsuario).stream()
                .sorted(Comparator.comparing(Orcamento::getGrupo))
                .map(OrcamentoResponse::from)
                .toList();
    }
}
