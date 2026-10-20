package com.ifo.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaRequest(
        @NotBlank(message = "obrigatório") @Size(max = 100, message = "máximo de 100 caracteres")
        String tituloMeta,

        @NotNull(message = "obrigatório") @Positive(message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "valor inválido")
        BigDecimal valorObjetivo,

        LocalDate prazo
) {}
