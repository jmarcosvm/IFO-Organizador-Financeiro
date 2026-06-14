package com.ifo.backend.repository;

import com.ifo.backend.model.ContaCarteira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContaRepository extends JpaRepository<ContaCarteira, Long> {
    List<ContaCarteira> findByIdUsuario(Long idUsuario);
}