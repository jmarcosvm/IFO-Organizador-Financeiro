package com.ifo.backend.repository;

import com.ifo.backend.model.MetaFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira, Long> {
    List<MetaFinanceira> findByUsuarioIdUsuarioOrderByIdMetaAsc(Long idUsuario);
    Optional<MetaFinanceira> findByIdMetaAndUsuarioIdUsuario(Long idMeta, Long idUsuario);
}
