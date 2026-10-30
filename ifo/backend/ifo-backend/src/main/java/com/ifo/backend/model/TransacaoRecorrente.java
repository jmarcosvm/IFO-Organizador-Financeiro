package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

// Modelo mensal de uma despesa (RF20): o gerador cria uma Transacao comum a cada mês
@Entity
@Table(name = "transacao_recorrente")
@Getter
@Setter
@NoArgsConstructor
public class TransacaoRecorrente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recorrencia")
    private Long idRecorrencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String descricao;

    @Column(name = "dia_mes", nullable = false)
    private Short diaMes;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false)
    private boolean ativa = true;

    // Dia 1 do último mês já gerado; é o que torna o gerador idempotente (RN15)
    @Column(name = "ultima_competencia")
    private LocalDate ultimaCompetencia;
}
