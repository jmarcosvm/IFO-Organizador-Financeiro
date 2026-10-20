package com.ifo.backend.dto;

import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.Orcamento;
import java.math.BigDecimal;

public record OrcamentoResponse(GrupoOrcamento grupo, BigDecimal percentual) {

    public static OrcamentoResponse from(Orcamento orcamento) {
        return new OrcamentoResponse(orcamento.getGrupo(), orcamento.getPercentual());
    }
}
