package com.ifo.backend.job;

import com.ifo.backend.service.RecorrenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class RecorrenciaJobTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 31);

    private RecorrenciaService service;
    private RecorrenciaJob job;

    @BeforeEach
    void setUp() {
        service = mock(RecorrenciaService.class);
        job = new RecorrenciaJob(service);
    }

    @Test
    void geraCadaRecorrenciaAtivaNaDataInformada() {
        when(service.idsAtivas()).thenReturn(List.of(1L, 2L));

        job.gerar(HOJE);

        verify(service).gerarPorId(1L, HOJE);
        verify(service).gerarPorId(2L, HOJE);
    }

    @Test
    void umaRecorrenciaComErroNaoImpedeAsOutras() {
        when(service.idsAtivas()).thenReturn(List.of(1L, 2L));
        when(service.gerarPorId(1L, HOJE)).thenThrow(new IllegalStateException("falhou"));

        assertDoesNotThrow(() -> job.gerar(HOJE));

        verify(service).gerarPorId(2L, HOJE);
    }

    @Test
    void semRecorrenciasAtivasNaoGeraNada() {
        when(service.idsAtivas()).thenReturn(List.of());

        job.gerar(HOJE);

        verify(service, never()).gerarPorId(anyLong(), any());
    }

    // Banco ainda acordando (Neon) na subida: a API precisa subir mesmo assim
    @Test
    void falhaAoListarNaSubidaNaoDerrubaAAplicacao() {
        when(service.idsAtivas()).thenThrow(new IllegalStateException("banco indisponível"));

        assertDoesNotThrow(() -> job.gerarAoSubir());
    }

    @Test
    void naSubidaGeraAsRecorrenciasAtivas() {
        when(service.idsAtivas()).thenReturn(List.of(7L));

        job.gerarAoSubir();

        verify(service).gerarPorId(eq(7L), any(LocalDate.class));
    }
}
