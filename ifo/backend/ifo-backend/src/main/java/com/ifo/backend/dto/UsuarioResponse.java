package com.ifo.backend.dto;

import com.ifo.backend.model.Usuario;
import java.math.BigDecimal;

public record UsuarioResponse(Long idUsuario, String nome, String email, BigDecimal rendaMensal) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getIdUsuario(), usuario.getNome(), usuario.getEmail(), usuario.getRendaMensal());
    }
}
