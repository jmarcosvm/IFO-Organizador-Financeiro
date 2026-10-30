package com.ifo.backend.service;

import com.ifo.backend.dto.AporteRequest;
import com.ifo.backend.dto.MetaRequest;
import com.ifo.backend.dto.MetaResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.exception.RegraNegocioException;
import com.ifo.backend.model.MetaFinanceira;
import com.ifo.backend.repository.MetaFinanceiraRepository;
import com.ifo.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetaFinanceiraServiceTest {

    private static final Long ID_USUARIO = 1L;
    private static final Long ID_META = 5L;

    private MetaFinanceiraRepository repository;
    private MetaFinanceiraService service;

    @BeforeEach
    void setUp() {
        repository = mock(MetaFinanceiraRepository.class);
        service = new MetaFinanceiraService(repository, mock(UsuarioRepository.class));
    }

    // Exemplo do contrato (06-api.md §6): 450 de 3000 = 15,0%
    @Test
    void aporteSomaAoValorAtualECalculaProgresso() {
        meta("3000.00", "0.00");

        MetaResponse resposta = service.aportar(ID_USUARIO, ID_META, new AporteRequest(new BigDecimal("450.00")));

        assertEquals(new BigDecimal("450.00"), resposta.valorAtual());
        assertEquals(new BigDecimal("15.0"), resposta.progresso());
        assertFalse(resposta.concluida());
    }

    @Test
    void aportesSeguidosAcumulam() {
        meta("3000.00", "0.00");

        service.aportar(ID_USUARIO, ID_META, new AporteRequest(new BigDecimal("100.00")));
        MetaResponse resposta = service.aportar(ID_USUARIO, ID_META, new AporteRequest(new BigDecimal("50.50")));

        assertEquals(new BigDecimal("150.50"), resposta.valorAtual());
    }

    @Test
    void aporteQueCompletaOObjetivoConcluiAMeta() {
        meta("3000.00", "2900.00");

        MetaResponse resposta = service.aportar(ID_USUARIO, ID_META, new AporteRequest(new BigDecimal("100.00")));

        assertTrue(resposta.concluida());
        assertEquals(0, new BigDecimal("100").compareTo(resposta.progresso()));
    }

    @Test
    void aporteAcimaDoObjetivoMantemProgressoEm100() {
        meta("3000.00", "2900.00");

        MetaResponse resposta = service.aportar(ID_USUARIO, ID_META, new AporteRequest(new BigDecimal("500.00")));

        assertEquals(new BigDecimal("3400.00"), resposta.valorAtual());
        assertTrue(resposta.concluida());
        assertEquals(0, new BigDecimal("100").compareTo(resposta.progresso()));
    }

    @Test
    void aporteQueEstouraOLimiteDoBancoFalhaSemAlterarAMeta() {
        MetaFinanceira meta = meta("3000.00", "9999999999.00");

        assertThrows(RegraNegocioException.class,
                () -> service.aportar(ID_USUARIO, ID_META, new AporteRequest(new BigDecimal("1.00"))));
        assertEquals(new BigDecimal("9999999999.00"), meta.getValorAtual());
    }

    @Test
    void aporteEmMetaDeOutroUsuarioDa404() {
        when(repository.findByIdMetaAndUsuarioIdUsuario(ID_META, 2L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.aportar(2L, ID_META, new AporteRequest(BigDecimal.TEN)));
        assertEquals("Meta não encontrada.", erro.getMessage());
    }

    @Test
    void editarMudaTituloObjetivoEPrazoEMantemOValorAtual() {
        MetaFinanceira meta = meta("3000.00", "450.00");

        MetaResponse resposta = service.atualizar(ID_USUARIO, ID_META,
                new MetaRequest("  Notebook novo ", new BigDecimal("1500.00"), LocalDate.of(2027, 6, 30)));

        assertEquals("Notebook novo", meta.getTituloMeta());
        assertEquals(new BigDecimal("1500.00"), resposta.valorObjetivo());
        assertEquals(LocalDate.of(2027, 6, 30), resposta.prazo());
        assertEquals(new BigDecimal("450.00"), resposta.valorAtual());
        assertEquals(new BigDecimal("30.0"), resposta.progresso());
    }

    @Test
    void reduzirOObjetivoAbaixoDoValorAtualConcluiAMeta() {
        meta("3000.00", "450.00");

        MetaResponse resposta = service.atualizar(ID_USUARIO, ID_META,
                new MetaRequest("Notebook", new BigDecimal("400.00"), null));

        assertTrue(resposta.concluida());
    }

    @Test
    void editarMetaDeOutroUsuarioDa404() {
        when(repository.findByIdMetaAndUsuarioIdUsuario(ID_META, 2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizar(2L, ID_META,
                new MetaRequest("Roubada", BigDecimal.TEN, null)));
    }

    private MetaFinanceira meta(String objetivo, String atual) {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setIdMeta(ID_META);
        meta.setTituloMeta("Notebook");
        meta.setValorObjetivo(new BigDecimal(objetivo));
        meta.setValorAtual(new BigDecimal(atual));
        when(repository.findByIdMetaAndUsuarioIdUsuario(ID_META, ID_USUARIO)).thenReturn(Optional.of(meta));
        return meta;
    }
}
