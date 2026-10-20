package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meta_financeira")
@Getter
@Setter
@NoArgsConstructor
public class MetaFinanceira {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_meta")
    private Long idMeta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "titulo_meta", nullable = false, length = 100)
    private String tituloMeta;

    @Column(name = "valor_objetivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorObjetivo;

    @Column(name = "valor_atual", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAtual = BigDecimal.ZERO;

    private LocalDate prazo;
}
