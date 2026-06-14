package com.ifo.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orcamento_mensal")
public class OrcamentoMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrcamento;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_orcamento", nullable = false)
    private TipoOrcamento tipoOrcamento; // NECESSIDADES, LAZER ou INVESTIMENTOS

    @Column(name = "limite_valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteValor;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    public OrcamentoMensal() {}

    public OrcamentoMensal(Long idUsuario, TipoOrcamento tipoOrcamento, BigDecimal limiteValor, Integer mes, Integer ano) {
        this.idUsuario = idUsuario;
        this.tipoOrcamento = tipoOrcamento;
        this.limiteValor = limiteValor;
        this.mes = mes;
        this.ano = ano;
    }

    // Getters e Setters
    public Long getIdOrcamento() { return idOrcamento; }
    public void setIdOrcamento(Long idOrcamento) { this.idOrcamento = idOrcamento; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public TipoOrcamento getTipoOrcamento() { return tipoOrcamento; }
    public void setTipoOrcamento(TipoOrcamento tipoOrcamento) { this.tipoOrcamento = tipoOrcamento; }

    public BigDecimal getLimiteValor() { return limiteValor; }
    public void setLimiteValor(BigDecimal limiteValor) { this.limiteValor = limiteValor; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
}