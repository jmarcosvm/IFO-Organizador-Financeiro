package com.ifo.backend.repository;

import com.ifo.backend.model.Transacao;
import com.ifo.backend.model.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByIdUsuario(Long idUsuario);
    List<Transacao> findByIdUsuarioAndTipo(Long idUsuario, TipoTransacao tipo);
}