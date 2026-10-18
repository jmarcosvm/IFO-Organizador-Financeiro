package com.ifo.backend.exception;

// 400: regra de negócio violada
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
