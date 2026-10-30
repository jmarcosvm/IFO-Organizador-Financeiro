package com.ifo.backend.dto;

import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.NivelOrcamento;
import java.math.BigDecimal;

// limite e percentualUsado vêm null quando não há como calcular (renda 0, ou grupo com 0%)
public record StatusGrupoResponse(GrupoOrcamento grupo, BigDecimal percentual, BigDecimal limite, BigDecimal gasto,
                                  BigDecimal percentualUsado, NivelOrcamento nivel) {}
