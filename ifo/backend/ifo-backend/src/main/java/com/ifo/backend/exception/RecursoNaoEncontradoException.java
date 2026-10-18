package com.ifo.backend.exception;

// 404: o recurso não existe ou não pertence ao usuário do token
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
