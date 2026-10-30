package com.ifo.backend.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AporteRequest(
        @NotNull(message = "obrigatório") @Positive(message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "valor inválido")
        BigDecimal valor
) {}
