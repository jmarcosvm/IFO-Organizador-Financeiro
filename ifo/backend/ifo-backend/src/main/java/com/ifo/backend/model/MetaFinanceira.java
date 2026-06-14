package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meta_financeira")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaFinanceira {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMeta;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "titulo_meta", nullable = false, length = 100)
    private String tituloMeta;

    @Column(name = "valor_objetivo", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorObjetivo;

    @Column(name = "valor_atual", precision = 10, scale = 2)
    private BigDecimal valorAtual = BigDecimal.ZERO;

    private LocalDate prazo;
}