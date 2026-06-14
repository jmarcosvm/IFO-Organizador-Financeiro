package com.ifo.backend.repository;

import com.ifo.backend.model.OrcamentoMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrcamentoRepository extends JpaRepository<OrcamentoMensal, Long> {
    List<OrcamentoMensal> findByIdUsuario(Long idUsuario);
}