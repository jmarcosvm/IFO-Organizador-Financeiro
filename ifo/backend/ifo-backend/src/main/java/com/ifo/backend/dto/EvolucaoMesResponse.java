package com.ifo.backend.dto;

import java.math.BigDecimal;

// Uma linha da tabela mês a mês: quanto saiu do bolso (investido) e quanto vale (montante)
public record EvolucaoMesResponse(int mes, BigDecimal investido, BigDecimal montante) {}
