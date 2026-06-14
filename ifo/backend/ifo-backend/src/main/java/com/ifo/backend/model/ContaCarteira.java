package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "conta_carteira")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContaCarteira {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConta;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "nome_conta", nullable = false, length = 50)
    private String nomeConta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false)
    private TipoConta tipoConta;

    @Column(name = "saldo_inicial", precision = 10, scale = 2)
    private BigDecimal saldoInicial = BigDecimal.ZERO;
}