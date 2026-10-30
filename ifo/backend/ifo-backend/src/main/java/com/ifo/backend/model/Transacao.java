package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Tipo e grupo não são gravados: vêm da categoria (RN02)
@Entity
@Table(name = "transacao")
@Getter
@Setter
@NoArgsConstructor
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transacao")
    private Long idTransacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    // Preenchido só nas transações geradas por uma recorrência; vira nulo se ela for excluída (RN16)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recorrencia")
    private TransacaoRecorrente recorrencia;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 255)
    private String descricao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
