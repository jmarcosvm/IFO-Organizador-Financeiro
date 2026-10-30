package com.ifo.backend.repository;

import com.ifo.backend.model.TransacaoRecorrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRecorrenteRepository extends JpaRepository<TransacaoRecorrente, Long> {
    List<TransacaoRecorrente> findByUsuarioIdUsuarioOrderByIdRecorrenciaAsc(Long idUsuario);
    Optional<TransacaoRecorrente> findByIdRecorrenciaAndUsuarioIdUsuario(Long idRecorrencia, Long idUsuario);

    // Só os ids: o job processa cada recorrência em sua própria transação
    @Query("select r.idRecorrencia from TransacaoRecorrente r where r.ativa = true order by r.idRecorrencia")
    List<Long> findIdsAtivas();
}
