package com.ifo.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecorrenciaRequest(
        @NotNull(message = "obrigatório")
        Short idCategoria,

        @NotNull(message = "obrigatório") @Positive(message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "valor inválido")
        BigDecimal valor,

        @Size(max = 255, message = "máximo de 255 caracteres")
        String descricao,

        @NotNull(message = "obrigatório") @Min(value = 1, message = "deve ser de 1 a 31") @Max(value = 31, message = "deve ser de 1 a 31")
        Short diaMes,

        @NotNull(message = "obrigatório")
        LocalDate dataInicio,

        LocalDate dataFim
) {}
