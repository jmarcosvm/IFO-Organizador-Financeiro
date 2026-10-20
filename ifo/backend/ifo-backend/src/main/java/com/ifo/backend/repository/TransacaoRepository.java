package com.ifo.backend.repository;

import com.ifo.backend.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByUsuarioIdUsuarioOrderByDataDescIdTransacaoDesc(Long idUsuario);
    Optional<Transacao> findByIdTransacaoAndUsuarioIdUsuario(Long idTransacao, Long idUsuario);
}
