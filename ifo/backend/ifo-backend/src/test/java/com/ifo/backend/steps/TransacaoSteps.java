package com.ifo.backend.steps;

import com.ifo.backend.dto.CadastroRequest;
import com.ifo.backend.dto.CategoriaResponse;
import com.ifo.backend.dto.DespesaGrupoResponse;
import com.ifo.backend.dto.ResumoResponse;
import com.ifo.backend.dto.TransacaoRequest;
import com.ifo.backend.dto.TransacaoResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.service.AuthService;
import com.ifo.backend.service.CategoriaService;
import com.ifo.backend.service.ResumoService;
import com.ifo.backend.service.TransacaoService;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransacaoSteps {

    @Autowired
    private AuthService authService;

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ResumoService resumoService;

    private Long idUsuario;
    private ResumoResponse resumo;
    private TransacaoResponse transacaoCriada;
    private TransacaoResponse transacaoEditada;
    private List<TransacaoResponse> lista;
    private String mensagemErro;
    private long tempoDecorrido;

    @Dado("que estou autenticado como um novo usuário")
    public void novo_usuario() {
        idUsuario = cadastrarUsuario();
    }

    @Quando("eu lanço {bigdecimal} na categoria {string}")
    @Dado("que eu lancei {bigdecimal} na categoria {string}")
    public void lancar(BigDecimal valor, String nomeCategoria) {
        transacaoCriada = transacaoService.criar(idUsuario, novaTransacao(valor, nomeCategoria, LocalDate.now()));
    }

    @Dado("que eu lancei {bigdecimal} na categoria {string} em {string}")
    public void lancar_em(BigDecimal valor, String nomeCategoria, String data) {
        transacaoService.criar(idUsuario, novaTransacao(valor, nomeCategoria, LocalDate.parse(data)));
    }

    @Quando("eu listo as transações de {int}\\/{int}")
    public void listar_mes(int mes, int ano) {
        lista = transacaoService.listar(idUsuario, YearMonth.of(ano, mes));
    }

    @Entao("todas devem ser de {int}\\/{int}")
    public void todas_do_mes(int mes, int ano) {
        assertTrue(lista.stream().allMatch(t -> YearMonth.from(t.data()).equals(YearMonth.of(ano, mes))));
    }

    @Entao("a transação deve ser salva com sucesso")
    public void transacao_salva() {
        assertNotNull(transacaoCriada.idTransacao(), "ID não foi gerado");
        assertTrue(transacaoCriada.valor().compareTo(BigDecimal.ZERO) > 0, "Valor deve ser positivo");
    }

    @Entao("o tipo derivado da categoria deve ser {string} sem grupo")
    public void tipo_sem_grupo(String tipo) {
        assertEquals(tipo, transacaoCriada.categoria().tipo().name());
        assertNull(transacaoCriada.categoria().grupo());
    }

    @Entao("o tipo derivado da categoria deve ser {string} no grupo {string}")
    public void tipo_com_grupo(String tipo, String grupo) {
        assertEquals(tipo, transacaoCriada.categoria().tipo().name());
        assertEquals(grupo, transacaoCriada.categoria().grupo().name());
    }

    @Quando("outro usuário tenta excluir essa transação")
    public void outro_usuario_exclui() {
        Long outroUsuario = cadastrarUsuario();
        try {
            transacaoService.excluir(outroUsuario, transacaoCriada.idTransacao());
        } catch (RecursoNaoEncontradoException e) {
            mensagemErro = e.getMessage();
        }
    }

    @Entao("a exclusão deve ser negada com a mensagem {string}")
    public void exclusao_negada(String mensagem) {
        assertEquals(mensagem, mensagemErro);
        assertEquals(1, transacaoService.listar(idUsuario, YearMonth.now()).size(), "A transação não deveria ter sido apagada");
    }

    @Quando("eu edito essa transação para {bigdecimal} na categoria {string}")
    public void editar(BigDecimal valor, String nomeCategoria) {
        transacaoEditada = transacaoService.atualizar(idUsuario, transacaoCriada.idTransacao(),
                novaTransacao(valor, nomeCategoria, LocalDate.now()));
    }

    @Entao("a transação deve manter o mesmo id")
    public void mesmo_id() {
        assertEquals(transacaoCriada.idTransacao(), transacaoEditada.idTransacao());
    }

    @Quando("outro usuário tenta editar essa transação para {bigdecimal} na categoria {string}")
    public void outro_usuario_edita(BigDecimal valor, String nomeCategoria) {
        Long outroUsuario = cadastrarUsuario();
        try {
            transacaoService.atualizar(outroUsuario, transacaoCriada.idTransacao(),
                    novaTransacao(valor, nomeCategoria, LocalDate.now()));
        } catch (RecursoNaoEncontradoException e) {
            mensagemErro = e.getMessage();
        }
    }

    @Entao("a edição deve ser negada com a mensagem {string}")
    public void edicao_negada(String mensagem) {
        assertEquals(mensagem, mensagemErro);
    }

    // Confere no banco (pela listagem), não só no objeto devolvido
    @Entao("ela deve estar com valor {bigdecimal} na categoria {string} no grupo {string}")
    public void conferir_no_banco(BigDecimal valor, String nomeCategoria, String grupo) {
        TransacaoResponse salva = transacaoService.listar(idUsuario, YearMonth.now()).stream()
                .filter(t -> t.idTransacao().equals(transacaoCriada.idTransacao()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, salva.valor().compareTo(valor));
        assertEquals(nomeCategoria, salva.categoria().nome());
        assertEquals(grupo, salva.categoria().grupo().name());
    }

    @Quando("eu peço o resumo de {int}\\/{int}")
    public void pedir_resumo(int mes, int ano) {
        resumo = resumoService.resumir(idUsuario, YearMonth.of(ano, mes));
    }

    @Entao("o resumo deve ter receitas {bigdecimal}; despesas {bigdecimal}; saldo {bigdecimal}")
    public void conferir_resumo(BigDecimal receitas, BigDecimal despesas, BigDecimal saldo) {
        assertEquals(0, resumo.totalReceitas().compareTo(receitas));
        assertEquals(0, resumo.totalDespesas().compareTo(despesas));
        assertEquals(0, resumo.saldo().compareTo(saldo));
    }

    @Entao("o grupo {string} deve somar {bigdecimal} e representar {bigdecimal}% das despesas")
    public void conferir_grupo(String grupo, BigDecimal total, BigDecimal percentual) {
        DespesaGrupoResponse doGrupo = resumo.despesasPorGrupo().stream()
                .filter(g -> g.grupo().name().equals(grupo))
                .findFirst()
                .orElseThrow();
        assertEquals(0, doGrupo.total().compareTo(total));
        assertEquals(0, doGrupo.percentual().compareTo(percentual));
    }

    @Dado("que eu tenho {int} transações lançadas")
    public void tenho_transacoes(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            transacaoService.criar(idUsuario, novaTransacao(BigDecimal.valueOf(100), "Alimentação", LocalDate.now()));
        }
    }

    @Quando("eu solicito a lista de transações")
    public void solicitar_lista() {
        long inicio = System.currentTimeMillis();
        lista = transacaoService.listar(idUsuario, YearMonth.now());
        tempoDecorrido = System.currentTimeMillis() - inicio;
    }

    @Entao("devo receber {int} transações")
    public void quantidade_recebida(int quantidade) {
        assertEquals(quantidade, lista.size());
    }

    @Entao("o tempo de resposta deve ser menor que {int} segundos")
    public void tempo_resposta(int segundos) {
        assertTrue(tempoDecorrido < segundos * 1000L,
                "Tempo excedido: " + tempoDecorrido + "ms (máximo: " + (segundos * 1000) + "ms)");
    }

    private Long cadastrarUsuario() {
        String email = "transacao-" + UUID.randomUUID() + "@ifo.teste";
        return authService.cadastrar(new CadastroRequest("Usuário Teste", email, "senhaSegura123", BigDecimal.valueOf(1500)))
                .usuario().idUsuario();
    }

    private TransacaoRequest novaTransacao(BigDecimal valor, String nomeCategoria, LocalDate data) {
        Short idCategoria = categoriaService.listar().stream()
                .filter(c -> c.nome().equals(nomeCategoria))
                .map(CategoriaResponse::idCategoria)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Categoria inexistente: " + nomeCategoria));
        return new TransacaoRequest(idCategoria, valor, data, "Transação de teste Cucumber");
    }
}
