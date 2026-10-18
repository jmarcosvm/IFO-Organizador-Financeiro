package com.ifo.backend.dto;

import com.ifo.backend.model.MetaFinanceira;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record MetaResponse(Long idMeta, String tituloMeta, BigDecimal valorObjetivo, BigDecimal valorAtual,
                           LocalDate prazo, BigDecimal progresso, boolean concluida) {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    // Progresso exibido limitado a 100% (RN09)
    public static MetaResponse from(MetaFinanceira meta) {
        BigDecimal progresso = meta.getValorAtual()
                .multiply(CEM)
                .divide(meta.getValorObjetivo(), 1, RoundingMode.HALF_UP)
                .min(CEM);
        boolean concluida = meta.getValorAtual().compareTo(meta.getValorObjetivo()) >= 0;
        return new MetaResponse(meta.getIdMeta(), meta.getTituloMeta(), meta.getValorObjetivo(), meta.getValorAtual(),
                meta.getPrazo(), progresso, concluida);
    }
}
