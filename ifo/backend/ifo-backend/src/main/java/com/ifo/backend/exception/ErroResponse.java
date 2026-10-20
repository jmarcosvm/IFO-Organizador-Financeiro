package com.ifo.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.Map;

// Formato único de erro da API (06-api.md §8)
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> campos
) {

    public static ErroResponse of(HttpStatus status, String mensagem) {
        return of(status, mensagem, null);
    }

    public static ErroResponse of(HttpStatus status, String mensagem, Map<String, String> campos) {
        return new ErroResponse(LocalDateTime.now(), status.value(), descricao(status), mensagem, campos);
    }

    private static String descricao(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case UNAUTHORIZED -> "Não autorizado";
            case NOT_FOUND -> "Não encontrado";
            case METHOD_NOT_ALLOWED -> "Método não permitido";
            case CONFLICT -> "Conflito";
            default -> "Erro interno";
        };
    }
}
