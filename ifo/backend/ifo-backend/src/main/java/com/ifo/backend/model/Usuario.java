package com.ifo.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Column(name = "renda_mensal", precision = 10, scale = 2)
    private BigDecimal rendaMensal = BigDecimal.ZERO;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();
}