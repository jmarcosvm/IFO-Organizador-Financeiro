package com.ifo.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "obrigatório") String email,
        @NotBlank(message = "obrigatório") String senha
) {}
