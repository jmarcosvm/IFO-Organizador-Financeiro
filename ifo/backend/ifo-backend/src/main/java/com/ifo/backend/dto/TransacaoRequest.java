package com.ifo.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

// Só a categoria é enviada: tipo e grupo vêm dela (RN02)
public record TransacaoRequest(
        @NotNull(message = "obrigatório")
        Short idCategoria,

        @NotNull(message = "obrigatório") @Positive(message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "valor inválido")
        BigDecimal valor,

        @NotNull(message = "obrigatório")
        LocalDate data,

        @Size(max = 255, message = "máximo de 255 caracteres")
        String descricao
) {}
