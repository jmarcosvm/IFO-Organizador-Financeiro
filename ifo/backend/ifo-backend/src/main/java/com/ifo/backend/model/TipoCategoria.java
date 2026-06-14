package com.ifo.backend.model;

public enum TipoCategoria {
    RENDA_MENSAL("Renda Mensal"),
    RENDA_EXTRA("Renda Extra"),
    LAZER("Lazer"),
    ALIMENTACAO("Alimentação"),
    TRANSPORTE("Transporte"),
    INVESTIMENTO("Investimento"),
    VESTUARIO("Vestuário"),
    OUTROS("Outros");

    private final String descricao;

    TipoCategoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}