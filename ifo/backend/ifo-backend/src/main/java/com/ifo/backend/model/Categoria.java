package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Categorias são fixas (carregadas pela migration V2); somente leitura
@Entity
@Table(name = "categoria")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    @Id
    @Column(name = "id_categoria")
    private Short idCategoria;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoCategoria tipo;

    // null para receitas
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private GrupoOrcamento grupo;
}
