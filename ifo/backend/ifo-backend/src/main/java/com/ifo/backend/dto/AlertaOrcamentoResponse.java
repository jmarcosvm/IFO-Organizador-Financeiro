package com.ifo.backend.dto;

import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.NivelOrcamento;
import java.math.BigDecimal;

// Aviso informativo (RN08): só existe quando o grupo está em ATENCAO ou ESTOURO
public record AlertaOrcamentoResponse(GrupoOrcamento grupo, NivelOrcamento nivel, BigDecimal percentualUsado,
                                      BigDecimal limite, BigDecimal gasto, String mensagem) {}
