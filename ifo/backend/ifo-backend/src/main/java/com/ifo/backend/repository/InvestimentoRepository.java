package com.ifo.backend.repository;

import com.ifo.backend.model.Investimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {
    List<Investimento> findByIdUsuario(Long idUsuario);
}