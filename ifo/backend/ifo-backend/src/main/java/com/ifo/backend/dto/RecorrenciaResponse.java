package com.ifo.backend.dto;

import com.ifo.backend.model.TransacaoRecorrente;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecorrenciaResponse(Long idRecorrencia, BigDecimal valor, String descricao, Short diaMes,
                                  LocalDate dataInicio, LocalDate dataFim, boolean ativa,
                                  LocalDate ultimaCompetencia, CategoriaResponse categoria) {

    public static RecorrenciaResponse from(TransacaoRecorrente r) {
        return new RecorrenciaResponse(r.getIdRecorrencia(), r.getValor(), r.getDescricao(), r.getDiaMes(),
                r.getDataInicio(), r.getDataFim(), r.isAtiva(), r.getUltimaCompetencia(),
                CategoriaResponse.from(r.getCategoria()));
    }
}
