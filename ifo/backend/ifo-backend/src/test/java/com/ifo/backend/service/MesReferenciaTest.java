package com.ifo.backend.service;

import com.ifo.backend.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class MesReferenciaTest {

    @Test
    void usaMesEAnoInformados() {
        assertEquals(YearMonth.of(2026, 9), MesReferencia.resolver(9, 2026));
    }

    @Test
    void semParametrosUsaMesAtual() {
        assertEquals(YearMonth.now(), MesReferencia.resolver(null, null));
    }

    @Test
    void parametroFaltandoVemDoMesAtual() {
        assertEquals(YearMonth.of(YearMonth.now().getYear(), 3), MesReferencia.resolver(3, null));
        assertEquals(YearMonth.of(2025, YearMonth.now().getMonthValue()), MesReferencia.resolver(null, 2025));
    }

    @Test
    void mesForaDoIntervaloEhRecusado() {
        assertThrows(RegraNegocioException.class, () -> MesReferencia.resolver(0, 2026));
        assertThrows(RegraNegocioException.class, () -> MesReferencia.resolver(13, 2026));
    }

    @Test
    void anoInvalidoEhRecusado() {
        assertThrows(RegraNegocioException.class, () -> MesReferencia.resolver(5, 1800));
    }
}
