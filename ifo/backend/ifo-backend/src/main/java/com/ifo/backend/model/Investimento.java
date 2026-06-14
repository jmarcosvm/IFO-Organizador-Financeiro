package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investimento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Investimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInvestimento;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "valor_mensal", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensal;

    @Column(name = "taxa_juros", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxaJuros;

    @Column(name = "tempo_meses", nullable = false)
    private Integer tempoMeses;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_juros")
    private TipoJuros tipoJuros = TipoJuros.COMPOSTO;

    @Column(name = "montante_final", precision = 10, scale = 2)
    private BigDecimal montanteFinal;

    @Column(name = "data_simulacao")
    private LocalDateTime dataSimulacao = LocalDateTime.now();
}
