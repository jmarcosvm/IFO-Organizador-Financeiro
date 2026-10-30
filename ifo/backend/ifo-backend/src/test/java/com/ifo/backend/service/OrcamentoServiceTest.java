package com.ifo.backend.service;

import com.ifo.backend.dto.AlertaOrcamentoResponse;
import com.ifo.backend.dto.DespesaGrupoResponse;
import com.ifo.backend.dto.OrcamentoRequest;
import com.ifo.backend.dto.ResumoResponse;
import com.ifo.backend.dto.StatusGrupoResponse;
import com.ifo.backend.exception.RegraNegocioException;
import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.NivelOrcamento;
import com.ifo.backend.model.Orcamento;
import com.ifo.backend.model.Usuario;
import com.ifo.backend.repository.OrcamentoRepository;
import com.ifo.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrcamentoServiceTest {

    private static final Long ID_USUARIO = 1L;
    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);
    private static final BigDecimal RENDA = new BigDecimal("1000.00");
    private static final BigDecimal TRINTA = new BigDecimal("30.00");

    private OrcamentoRepository repository;
    private UsuarioRepository usuarioRepository;
    private ResumoService resumoService;
    private OrcamentoService service;

    @BeforeEach
    void setUp() {
        repository = mock(OrcamentoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        resumoService = mock(ResumoService.class);
        service = new OrcamentoService(repository, usuarioRepository, resumoService);
    }

    // ---- níveis (RN07): renda 1000 e Lazer 30% => limite 300,00 ----

    @Test
    void abaixoDe80PorCentoEhOk() {
        assertNivel(NivelOrcamento.OK, "239.70"); // 79,9%
    }

    @Test
    void exatamente80PorCentoEhAtencao() {
        assertNivel(NivelOrcamento.ATENCAO, "240.00");
    }

    @Test
    void exatamente100PorCentoAindaEhAtencao() {
        assertNivel(NivelOrcamento.ATENCAO, "300.00");
    }

    @Test
    void acimaDe100PorCentoEhEstouro() {
        assertNivel(NivelOrcamento.ESTOURO, "300.01"); // 100,003%: o % arredondado mostra 100,0
    }

    @Test
    void semGastoEhOk() {
        assertNivel(NivelOrcamento.OK, "0.00");
    }

    @Test
    void calculaLimiteEPercentualUsado() {
        StatusGrupoResponse status = status("380.00", RENDA);

        assertEquals(new BigDecimal("300.00"), status.limite());
        assertEquals(new BigDecimal("126.7"), status.percentualUsado());
        assertEquals(new BigDecimal("380.00"), status.gasto());
    }

    // ---- renda zero (RN06) ----

    @Test
    void rendaZeroNaoTemLimiteNemAviso() {
        StatusGrupoResponse status = status("500.00", BigDecimal.ZERO);

        assertEquals(NivelOrcamento.SEM_LIMITE, status.nivel());
        assertNull(status.limite());
        assertNull(status.percentualUsado());
    }

    @Test
    void grupoComZeroPorCentoComGastoEstoura() {
        StatusGrupoResponse status = OrcamentoService.calcularStatus(
                GrupoOrcamento.LAZER, BigDecimal.ZERO, RENDA, new BigDecimal("10.00"));

        assertEquals(NivelOrcamento.ESTOURO, status.nivel());
        assertNull(status.percentualUsado());
    }

    @Test
    void grupoComZeroPorCentoSemGastoEhOk() {
        StatusGrupoResponse status = OrcamentoService.calcularStatus(
                GrupoOrcamento.LAZER, BigDecimal.ZERO, RENDA, BigDecimal.ZERO);

        assertEquals(NivelOrcamento.OK, status.nivel());
    }

    // ---- soma dos percentuais (RN05) ----

    @Test
    void somaDiferenteDe100Falha() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> service.atualizar(ID_USUARIO,
                new OrcamentoRequest(new BigDecimal("50"), new BigDecimal("30"), new BigDecimal("30"))));

        assertEquals("A soma dos percentuais deve ser 100.", erro.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void somaIgual100AtualizaOsTresGrupos() {
        when(repository.findByUsuarioIdUsuario(ID_USUARIO)).thenReturn(List.of(
                orcamento(GrupoOrcamento.NECESSIDADES, "50"),
                orcamento(GrupoOrcamento.LAZER, "30"),
                orcamento(GrupoOrcamento.INVESTIMENTOS, "20")));

        service.atualizar(ID_USUARIO,
                new OrcamentoRequest(new BigDecimal("60"), new BigDecimal("25.5"), new BigDecimal("14.5")));

        ArgumentCaptor<Orcamento> salvos = ArgumentCaptor.forClass(Orcamento.class);
        verify(repository, times(3)).save(salvos.capture());
        assertEquals(new BigDecimal("60"), percentualDe(salvos, GrupoOrcamento.NECESSIDADES));
        assertEquals(new BigDecimal("25.5"), percentualDe(salvos, GrupoOrcamento.LAZER));
        assertEquals(new BigDecimal("14.5"), percentualDe(salvos, GrupoOrcamento.INVESTIMENTOS));
    }

    @Test
    void criaPadrao503020ParaUsuarioNovo() {
        service.criarPadrao(new Usuario());

        ArgumentCaptor<Orcamento> salvos = ArgumentCaptor.forClass(Orcamento.class);
        verify(repository, times(3)).save(salvos.capture());
        assertEquals(new BigDecimal("50"), percentualDe(salvos, GrupoOrcamento.NECESSIDADES));
        assertEquals(new BigDecimal("30"), percentualDe(salvos, GrupoOrcamento.LAZER));
        assertEquals(new BigDecimal("20"), percentualDe(salvos, GrupoOrcamento.INVESTIMENTOS));
    }

    // ---- alerta (RF14, RN08) ----

    @Test
    void alertaDeEstouroTrazMensagemDoGrupo() {
        prepararMes("350.00", RENDA);

        AlertaOrcamentoResponse alerta = service.alertar(ID_USUARIO, GrupoOrcamento.LAZER, OUTUBRO);

        assertNotNull(alerta);
        assertEquals(NivelOrcamento.ESTOURO, alerta.nivel());
        assertEquals("Você ultrapassou o limite de Lazer deste mês.", alerta.mensagem());
    }

    @Test
    void alertaDeAtencaoInformaOPercentualUsado() {
        prepararMes("252.00", RENDA);

        AlertaOrcamentoResponse alerta = service.alertar(ID_USUARIO, GrupoOrcamento.LAZER, OUTUBRO);

        assertEquals(NivelOrcamento.ATENCAO, alerta.nivel());
        assertEquals("Você já usou 84,0% do limite de Lazer deste mês.", alerta.mensagem());
    }

    @Test
    void semAlertaQuandoTudoOk() {
        prepararMes("100.00", RENDA);

        assertNull(service.alertar(ID_USUARIO, GrupoOrcamento.LAZER, OUTUBRO));
    }

    @Test
    void semAlertaParaReceita() {
        assertNull(service.alertar(ID_USUARIO, null, OUTUBRO));
        verifyNoInteractions(resumoService);
    }

    @Test
    void semAlertaQuandoRendaEhZero() {
        prepararMes("500.00", BigDecimal.ZERO);

        assertNull(service.alertar(ID_USUARIO, GrupoOrcamento.LAZER, OUTUBRO));
    }

    // ---- apoio ----

    private void assertNivel(NivelOrcamento esperado, String gasto) {
        assertEquals(esperado, status(gasto, RENDA).nivel());
    }

    private StatusGrupoResponse status(String gasto, BigDecimal renda) {
        return OrcamentoService.calcularStatus(GrupoOrcamento.LAZER, TRINTA, renda, new BigDecimal(gasto));
    }

    // Monta o cenário: orçamento 50/30/20, a renda dada e o gasto em Lazer
    private void prepararMes(String gastoLazer, BigDecimal renda) {
        Usuario usuario = new Usuario();
        usuario.setRendaMensal(renda);
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuario));
        when(repository.findByUsuarioIdUsuario(ID_USUARIO)).thenReturn(List.of(
                orcamento(GrupoOrcamento.NECESSIDADES, "50"),
                orcamento(GrupoOrcamento.LAZER, "30"),
                orcamento(GrupoOrcamento.INVESTIMENTOS, "20")));
        when(resumoService.resumir(ID_USUARIO, OUTUBRO)).thenReturn(new ResumoResponse(10, 2026,
                BigDecimal.ZERO, new BigDecimal(gastoLazer), BigDecimal.ZERO, List.of(
                new DespesaGrupoResponse(GrupoOrcamento.NECESSIDADES, BigDecimal.ZERO, BigDecimal.ZERO),
                new DespesaGrupoResponse(GrupoOrcamento.LAZER, new BigDecimal(gastoLazer), BigDecimal.ZERO),
                new DespesaGrupoResponse(GrupoOrcamento.INVESTIMENTOS, BigDecimal.ZERO, BigDecimal.ZERO))));
    }

    private Orcamento orcamento(GrupoOrcamento grupo, String percentual) {
        Orcamento orcamento = new Orcamento();
        orcamento.setGrupo(grupo);
        orcamento.setPercentual(new BigDecimal(percentual));
        return orcamento;
    }

    private BigDecimal percentualDe(ArgumentCaptor<Orcamento> salvos, GrupoOrcamento grupo) {
        return salvos.getAllValues().stream()
                .filter(o -> o.getGrupo() == grupo)
                .findFirst()
                .orElseThrow()
                .getPercentual();
    }
}
