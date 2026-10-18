package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

// Um registro por (usuário, grupo); o limite em R$ é renda × percentual
@Entity
@Table(name = "orcamento")
@Getter
@Setter
@NoArgsConstructor
public class Orcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orcamento")
    private Long idOrcamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private GrupoOrcamento grupo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentual;
}
