package com.ifo.backend.service;

import com.ifo.backend.dto.EvolucaoMesResponse;
import com.ifo.backend.dto.SimulacaoRequest;
import com.ifo.backend.dto.SimulacaoResponse;
import com.ifo.backend.model.TipoJuros;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// Simulador de investimentos (RF18–RF19, RN13): nada é salvo no banco (ADR-003).
// Aporte no fim de cada mês; as contas ficam em precisão alta e só o resultado vai a 2 casas.
@Service
public class SimuladorService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal DOIS = BigDecimal.valueOf(2);
    private static final MathContext PRECISAO = MathContext.DECIMAL128;

    public SimulacaoResponse simular(SimulacaoRequest request) {
        BigDecimal inicial = request.aplicacaoInicial() != null ? request.aplicacaoInicial() : BigDecimal.ZERO;
        BigDecimal mensal = request.valorMensal();
        BigDecimal taxa = request.taxaMensal().divide(CEM, PRECISAO);
        int meses = request.tempoMeses();

        List<EvolucaoMesResponse> evolucao = new ArrayList<>(meses);
        for (int mes = 1; mes <= meses; mes++) {
            BigDecimal montante = request.tipoJuros() == TipoJuros.COMPOSTO
                    ? composto(inicial, mensal, taxa, mes)
                    : simples(inicial, mensal, taxa, mes);
            evolucao.add(new EvolucaoMesResponse(mes, investido(inicial, mensal, mes), arredondar(montante)));
        }

        EvolucaoMesResponse ultimo = evolucao.get(meses - 1);
        return new SimulacaoResponse(ultimo.montante(), ultimo.investido(),
                ultimo.montante().subtract(ultimo.investido()), evolucao);
    }

    // M = P·(1+i)^n + PMT·((1+i)^n − 1) / i   (i = 0: M = P + PMT·n)
    private BigDecimal composto(BigDecimal inicial, BigDecimal mensal, BigDecimal taxa, int meses) {
        if (taxa.signum() == 0) {
            return inicial.add(mensal.multiply(BigDecimal.valueOf(meses)));
        }
        BigDecimal fator = BigDecimal.ONE.add(taxa).pow(meses, PRECISAO);
        BigDecimal aportes = mensal.multiply(fator.subtract(BigDecimal.ONE)).divide(taxa, PRECISAO);
        return inicial.multiply(fator).add(aportes);
    }

    // M = P·(1 + i·n) + PMT·n + PMT·i·n·(n−1)/2
    private BigDecimal simples(BigDecimal inicial, BigDecimal mensal, BigDecimal taxa, int meses) {
        BigDecimal n = BigDecimal.valueOf(meses);
        BigDecimal jurosInicial = inicial.multiply(BigDecimal.ONE.add(taxa.multiply(n)));
        BigDecimal jurosAportes = mensal.multiply(taxa).multiply(n).multiply(n.subtract(BigDecimal.ONE))
                .divide(DOIS, PRECISAO);
        return jurosInicial.add(mensal.multiply(n)).add(jurosAportes);
    }

    // Total investido = P + PMT·n
    private BigDecimal investido(BigDecimal inicial, BigDecimal mensal, int meses) {
        return arredondar(inicial.add(mensal.multiply(BigDecimal.valueOf(meses))));
    }

    private BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
