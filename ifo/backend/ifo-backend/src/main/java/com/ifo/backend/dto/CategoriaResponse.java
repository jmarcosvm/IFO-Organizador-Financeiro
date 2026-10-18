package com.ifo.backend.dto;

import com.ifo.backend.model.Categoria;
import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.TipoCategoria;

public record CategoriaResponse(Short idCategoria, String nome, TipoCategoria tipo, GrupoOrcamento grupo) {

    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getIdCategoria(), categoria.getNome(), categoria.getTipo(), categoria.getGrupo());
    }
}
