package com.ifo.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AtivaRequest(
        @NotNull(message = "obrigatório")
        Boolean ativa
) {}
