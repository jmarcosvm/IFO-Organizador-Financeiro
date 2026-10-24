package com.ifo.backend.service;

import com.ifo.backend.exception.RegraNegocioException;
import java.time.YearMonth;

// Converte os parâmetros ?mes=&ano= em um mês de referência; o que faltar vem do mês atual
public final class MesReferencia {

    private MesReferencia() {
    }

    public static YearMonth resolver(Integer mes, Integer ano) {
        YearMonth atual = YearMonth.now();
        int mesFinal = mes != null ? mes : atual.getMonthValue();
        int anoFinal = ano != null ? ano : atual.getYear();

        if (mesFinal < 1 || mesFinal > 12) {
            throw new RegraNegocioException("O mês deve estar entre 1 e 12.");
        }
        if (anoFinal < 1900 || anoFinal > 9999) {
            throw new RegraNegocioException("Ano inválido.");
        }
        return YearMonth.of(anoFinal, mesFinal);
    }
}
