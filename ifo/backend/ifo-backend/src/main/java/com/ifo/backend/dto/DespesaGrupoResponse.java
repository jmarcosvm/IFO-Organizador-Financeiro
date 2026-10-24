package com.ifo.backend.dto;

import com.ifo.backend.model.GrupoOrcamento;
import java.math.BigDecimal;

// percentual = quanto o grupo representa do total de despesas do mês
public record DespesaGrupoResponse(GrupoOrcamento grupo, BigDecimal total, BigDecimal percentual) {}
