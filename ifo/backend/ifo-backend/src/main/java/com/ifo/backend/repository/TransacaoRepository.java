package com.ifo.backend.repository;

import com.ifo.backend.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    // Usa o índice (id_usuario, data) criado na migration V1
    List<Transacao> findByUsuarioIdUsuarioAndDataBetweenOrderByDataDescIdTransacaoDesc(
            Long idUsuario, LocalDate inicio, LocalDate fim);
    Optional<Transacao> findByIdTransacaoAndUsuarioIdUsuario(Long idTransacao, Long idUsuario);
}
