package com.ifo.backend.model;

public enum TipoOrcamento {
    NECESSIDADES("Necessidades"),
    LAZER("Lazer"),
    INVESTIMENTOS("Investimentos");

    private final String descricao;

    TipoOrcamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}