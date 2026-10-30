package com.ifo.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record SimulacaoResponse(BigDecimal montanteFinal, BigDecimal totalInvestido, BigDecimal rendimento,
                                List<EvolucaoMesResponse> evolucao) {}
