package com.ifo.backend.service;

import com.ifo.backend.dto.RecorrenciaRequest;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.exception.RegraNegocioException;
import com.ifo.backend.model.Categoria;
import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.TipoCategoria;
import com.ifo.backend.model.Transacao;
import com.ifo.backend.model.TransacaoRecorrente;
import com.ifo.backend.repository.TransacaoRecorrenteRepository;
import com.ifo.backend.repository.TransacaoRepository;
import com.ifo.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecorrenciaServiceTest {

    private static final Long ID_USUARIO = 1L;
    private static final Long ID_RECORRENCIA = 5L;
    private static final Categoria CONTAS_FIXAS =
            new Categoria((short) 8, "Contas fixas", TipoCategoria.DESPESA, GrupoOrcamento.NECESSIDADES);
    private static final Categoria SALARIO = new Categoria((short) 1, "Salário", TipoCategoria.RECEITA, null);

    private TransacaoRecorrenteRepository repository;
    private TransacaoRepository transacaoRepository;
    private CategoriaService categoriaService;
    private RecorrenciaService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransacaoRecorrenteRepository.class);
        transacaoRepository = mock(TransacaoRepository.class);
        categoriaService = mock(CategoriaService.class);
        service = new RecorrenciaService(repository, transacaoRepository, mock(UsuarioRepository.class),
                categoriaService);
    }

    @Test
    void geraUmaTransacaoPorMesAteOMesAtual() {
        TransacaoRecorrente r = recorrencia(10, "2026-08-01", null);

        int geradas = service.gerar(r, LocalDate.of(2026, 10, 15));

        assertEquals(3, geradas);
        assertEquals(List.of(LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 10), LocalDate.of(2026, 10, 10)),
                datasGeradas());
        assertEquals(LocalDate.of(2026, 10, 1), r.getUltimaCompetencia());
    }

    @Test
    void transacaoGeradaCopiaOsDadosEFicaVinculadaARecorrencia() {
        TransacaoRecorrente r = recorrencia(10, "2026-10-01", null);

        service.gerar(r, LocalDate.of(2026, 10, 15));

        Transacao t = transacoesGeradas().get(0);
        assertSame(r, t.getRecorrencia());
        assertEquals(new BigDecimal("99.90"), t.getValor());
        assertEquals("internet", t.getDescricao());
        assertSame(CONTAS_FIXAS, t.getCategoria());
    }

    @Test
    void dia31UsaOUltimoDiaDosMesesCurtos() {
        TransacaoRecorrente r = recorrencia(31, "2027-01-01", null);

        service.gerar(r, LocalDate.of(2027, 4, 30));

        assertEquals(List.of(LocalDate.of(2027, 1, 31), LocalDate.of(2027, 2, 28),
                LocalDate.of(2027, 3, 31), LocalDate.of(2027, 4, 30)), datasGeradas());
    }

    @Test
    void dia31EmFevereiroDeAnoBissextoViraDia29() {
        TransacaoRecorrente r = recorrencia(31, "2028-02-01", null);

        service.gerar(r, LocalDate.of(2028, 2, 29));

        assertEquals(List.of(LocalDate.of(2028, 2, 29)), datasGeradas());
    }

    @Test
    void recuperaMesesPerdidosAPartirDaUltimaCompetencia() {
        TransacaoRecorrente r = recorrencia(10, "2026-01-01", null);
        r.setUltimaCompetencia(LocalDate.of(2026, 7, 1));

        int geradas = service.gerar(r, LocalDate.of(2026, 10, 20));

        assertEquals(3, geradas);
        assertEquals(LocalDate.of(2026, 8, 10), datasGeradas().get(0));
    }

    @Test
    void rodarDuasVezesNaoDuplica() {
        TransacaoRecorrente r = recorrencia(10, "2026-08-01", null);
        LocalDate hoje = LocalDate.of(2026, 10, 15);

        assertEquals(3, service.gerar(r, hoje));
        assertEquals(0, service.gerar(r, hoje));

        verify(transacaoRepository, times(3)).save(any(Transacao.class));
    }

    @Test
    void ocorrenciaQueAindaNaoChegouNaoGeraENemAvancaACompetencia() {
        TransacaoRecorrente r = recorrencia(20, "2026-10-01", null);

        assertEquals(0, service.gerar(r, LocalDate.of(2026, 10, 15)));
        assertNull(r.getUltimaCompetencia());

        assertEquals(1, service.gerar(r, LocalDate.of(2026, 10, 20)));
        assertEquals(List.of(LocalDate.of(2026, 10, 20)), datasGeradas());
    }

    @Test
    void recorrenciaPausadaNaoGera() {
        TransacaoRecorrente r = recorrencia(10, "2026-08-01", null);
        r.setAtiva(false);

        assertEquals(0, service.gerar(r, LocalDate.of(2026, 10, 15)));
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void naoGeraDepoisDaDataFim() {
        TransacaoRecorrente r = recorrencia(10, "2026-07-01", "2026-09-30");

        assertEquals(3, service.gerar(r, LocalDate.of(2026, 12, 15)));
        assertEquals(LocalDate.of(2026, 9, 10), datasGeradas().get(2));
    }

    @Test
    void ocorrenciaDeAntesDoInicioNoPrimeiroMesEPulada() {
        TransacaoRecorrente r = recorrencia(10, "2026-08-20", null);

        service.gerar(r, LocalDate.of(2026, 10, 15));

        assertEquals(List.of(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 10, 10)), datasGeradas());
    }

    @Test
    void reativarNaoGeraOsMesesEmQueEstevePausada() {
        TransacaoRecorrente r = recorrencia(10, "2026-01-01", null);
        r.setUltimaCompetencia(LocalDate.of(2026, 3, 1));
        r.setAtiva(false);
        when(repository.findByIdRecorrenciaAndUsuarioIdUsuario(ID_RECORRENCIA, ID_USUARIO))
                .thenReturn(Optional.of(r));

        // hoje real do teste: só o mês atual pode ser gerado, nunca abril em diante
        service.definirAtiva(ID_USUARIO, ID_RECORRENCIA, true);

        assertTrue(r.isAtiva());
        LocalDate primeiroDiaDoMesAtual = LocalDate.now().withDayOfMonth(1);
        for (LocalDate data : datasGeradas()) {
            assertFalse(data.isBefore(primeiroDiaDoMesAtual), "gerou mês em que estava pausada: " + data);
        }
    }

    @Test
    void criarJaGeraAsOcorrenciasVencidas() {
        when(categoriaService.buscarEntidade((short) 8)).thenReturn(CONTAS_FIXAS);
        LocalDate inicio = LocalDate.now().minusMonths(2).withDayOfMonth(1);

        var resposta = service.criar(ID_USUARIO,
                new RecorrenciaRequest((short) 8, new BigDecimal("99.90"), "internet", (short) 1, inicio, null));

        assertEquals(3, datasGeradas().size());
        assertEquals(LocalDate.now().withDayOfMonth(1), resposta.ultimaCompetencia());
    }

    @Test
    void categoriaDeReceitaNaoPodeSerRecorrente() {
        when(categoriaService.buscarEntidade((short) 1)).thenReturn(SALARIO);

        assertThrows(RegraNegocioException.class, () -> service.criar(ID_USUARIO,
                new RecorrenciaRequest((short) 1, BigDecimal.TEN, null, (short) 5, LocalDate.of(2026, 10, 1), null)));
        verify(repository, never()).save(any());
    }

    @Test
    void dataFimAnteriorAoInicioEhRecusada() {
        when(categoriaService.buscarEntidade((short) 8)).thenReturn(CONTAS_FIXAS);

        assertThrows(RegraNegocioException.class, () -> service.criar(ID_USUARIO,
                new RecorrenciaRequest((short) 8, BigDecimal.TEN, null, (short) 5,
                        LocalDate.of(2026, 10, 1), LocalDate.of(2026, 9, 30))));
    }

    @Test
    void recorrenciaDeOutroUsuarioDa404() {
        when(repository.findByIdRecorrenciaAndUsuarioIdUsuario(ID_RECORRENCIA, 2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(2L, ID_RECORRENCIA));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.definirAtiva(2L, ID_RECORRENCIA, false));
    }

    private TransacaoRecorrente recorrencia(int dia, String inicio, String fim) {
        TransacaoRecorrente r = new TransacaoRecorrente();
        r.setIdRecorrencia(ID_RECORRENCIA);
        r.setCategoria(CONTAS_FIXAS);
        r.setValor(new BigDecimal("99.90"));
        r.setDescricao("internet");
        r.setDiaMes((short) dia);
        r.setDataInicio(LocalDate.parse(inicio));
        r.setDataFim(fim != null ? LocalDate.parse(fim) : null);
        return r;
    }

    private List<Transacao> transacoesGeradas() {
        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository, atLeast(0)).save(captor.capture());
        return captor.getAllValues();
    }

    private List<LocalDate> datasGeradas() {
        return transacoesGeradas().stream().map(Transacao::getData).toList();
    }
}
