package com.ifo.backend.dto;

import com.ifo.backend.model.TipoJuros;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

// aplicacaoInicial é opcional (vazio = 0); taxaMensal em % (1.0 = 1% ao mês)
public record SimulacaoRequest(
        @PositiveOrZero(message = "não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "valor inválido")
        BigDecimal aplicacaoInicial,

        @NotNull(message = "obrigatório") @PositiveOrZero(message = "não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "valor inválido")
        BigDecimal valorMensal,

        @NotNull(message = "obrigatório") @PositiveOrZero(message = "não pode ser negativo")
        @DecimalMax(value = "100", message = "máximo de 100")
        @Digits(integer = 3, fraction = 4, message = "valor inválido")
        BigDecimal taxaMensal,

        @NotNull(message = "obrigatório") @Min(value = 1, message = "mínimo de 1 mês")
        @Max(value = 1200, message = "máximo de 1200 meses")
        Integer tempoMeses,

        @NotNull(message = "obrigatório")
        TipoJuros tipoJuros
) {}
