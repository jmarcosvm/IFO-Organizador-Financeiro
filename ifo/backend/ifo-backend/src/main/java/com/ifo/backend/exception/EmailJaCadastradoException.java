package com.ifo.backend.exception;

// 409: e-mail já cadastrado
public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}
