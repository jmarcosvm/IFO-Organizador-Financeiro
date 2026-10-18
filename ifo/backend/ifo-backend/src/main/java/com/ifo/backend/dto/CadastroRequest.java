package com.ifo.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CadastroRequest(
        @NotBlank(message = "obrigatório") @Size(max = 100, message = "máximo de 100 caracteres")
        String nome,

        @NotBlank(message = "obrigatório") @Email(message = "e-mail inválido") @Size(max = 150, message = "máximo de 150 caracteres")
        String email,

        // BCrypt usa no máximo 72 bytes da senha
        @NotBlank(message = "obrigatório") @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres")
        String senha,

        @PositiveOrZero(message = "não pode ser negativa") @Digits(integer = 10, fraction = 2, message = "valor inválido")
        BigDecimal rendaMensal
) {}
