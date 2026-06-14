package com.ifo.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private TipoCategoria tipo;

    @Column(nullable = false)
    private String descricao;

    // Getters e Setters
    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public TipoCategoria getTipo() { return tipo; }
    public void setTipo(TipoCategoria tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}