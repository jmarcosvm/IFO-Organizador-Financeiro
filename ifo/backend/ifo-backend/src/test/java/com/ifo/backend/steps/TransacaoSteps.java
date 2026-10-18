package com.ifo.backend.steps;

import com.ifo.backend.dto.CadastroRequest;
import com.ifo.backend.dto.CategoriaResponse;
import com.ifo.backend.dto.TransacaoRequest;
import com.ifo.backend.dto.TransacaoResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.service.AuthService;
import com.ifo.backend.service.CategoriaService;
import com.ifo.backend.service.TransacaoService;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    private Long idUsuario;
    private TransacaoResponse transacaoCriada;
    private List<TransacaoResponse> lista;
    private String mensagemErro;
    private long tempoDecorrido;

    @Dado("que estou autenticado como um novo usuário")
    public void novo_usuario() {
        idUsuario = cadastrarUsuario();
    }

    @Quando("eu lanço {double} na categoria {string}")
    @Dado("que eu lancei {double} na categoria {string}")
    public void lancar(double valor, String nomeCategoria) {
        transacaoCriada = transacaoService.criar(idUsuario, novaTransacao(valor, nomeCategoria));
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
        assertEquals(1, transacaoService.listar(idUsuario).size(), "A transação não deveria ter sido apagada");
    }

    @Dado("que eu tenho {int} transações lançadas")
    public void tenho_transacoes(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            transacaoService.criar(idUsuario, novaTransacao(100, "Alimentação"));
        }
    }

    @Quando("eu solicito a lista de transações")
    public void solicitar_lista() {
        long inicio = System.currentTimeMillis();
        lista = transacaoService.listar(idUsuario);
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

    private TransacaoRequest novaTransacao(double valor, String nomeCategoria) {
        Short idCategoria = categoriaService.listar().stream()
                .filter(c -> c.nome().equals(nomeCategoria))
                .map(CategoriaResponse::idCategoria)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Categoria inexistente: " + nomeCategoria));
        return new TransacaoRequest(idCategoria, BigDecimal.valueOf(valor), LocalDate.now(), "Transação de teste Cucumber");
    }
}
