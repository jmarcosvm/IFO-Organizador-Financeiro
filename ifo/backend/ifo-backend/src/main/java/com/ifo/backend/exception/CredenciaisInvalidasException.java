package com.ifo.backend.exception;

// 401: e-mail ou senha incorretos
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
