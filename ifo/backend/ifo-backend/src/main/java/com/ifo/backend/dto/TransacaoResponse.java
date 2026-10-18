package com.ifo.backend.dto;

import com.ifo.backend.model.Transacao;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(Long idTransacao, BigDecimal valor, LocalDate data, String descricao, CategoriaResponse categoria) {

    public static TransacaoResponse from(Transacao transacao) {
        return new TransacaoResponse(transacao.getIdTransacao(), transacao.getValor(), transacao.getData(),
                transacao.getDescricao(), CategoriaResponse.from(transacao.getCategoria()));
    }
}
