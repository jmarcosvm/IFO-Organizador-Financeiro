package com.ifo.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

// Corpo do PUT /orcamento: { "NECESSIDADES": 50, "LAZER": 30, "INVESTIMENTOS": 20 }
// A soma = 100 é regra de negócio e fica no OrcamentoService
public record OrcamentoRequest(
        @JsonProperty("NECESSIDADES")
        @NotNull(message = "obrigatório") @DecimalMin(value = "0", message = "não pode ser negativo")
        @DecimalMax(value = "100", message = "máximo de 100") @Digits(integer = 3, fraction = 2, message = "valor inválido")
        BigDecimal necessidades,

        @JsonProperty("LAZER")
        @NotNull(message = "obrigatório") @DecimalMin(value = "0", message = "não pode ser negativo")
        @DecimalMax(value = "100", message = "máximo de 100") @Digits(integer = 3, fraction = 2, message = "valor inválido")
        BigDecimal lazer,

        @JsonProperty("INVESTIMENTOS")
        @NotNull(message = "obrigatório") @DecimalMin(value = "0", message = "não pode ser negativo")
        @DecimalMax(value = "100", message = "máximo de 100") @Digits(integer = 3, fraction = 2, message = "valor inválido")
        BigDecimal investimentos
) {}
