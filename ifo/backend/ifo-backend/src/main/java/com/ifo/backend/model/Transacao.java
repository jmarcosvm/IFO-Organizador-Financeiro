package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransacao;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_conta", nullable = false)
    private Long idConta;

    @Column(nullable = false)
    private Long idCategoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCategoria categoriaTipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 255)
    private String descricao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private Boolean paga = false;
}