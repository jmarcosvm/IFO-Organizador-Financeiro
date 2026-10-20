package com.ifo.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AtualizarUsuarioRequest(
        @NotBlank(message = "obrigatório") @Size(max = 100, message = "máximo de 100 caracteres")
        String nome,

        @NotNull(message = "obrigatório") @PositiveOrZero(message = "não pode ser negativa")
        @Digits(integer = 10, fraction = 2, message = "valor inválido")
        BigDecimal rendaMensal
) {}
