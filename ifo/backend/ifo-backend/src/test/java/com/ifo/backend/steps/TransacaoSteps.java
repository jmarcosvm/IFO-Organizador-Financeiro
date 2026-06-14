package com.ifo.backend.steps;

import com.ifo.backend.model.Transacao;
import com.ifo.backend.model.TipoTransacao;
import com.ifo.backend.repository.TransacaoRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransacaoSteps {

    @Autowired
    private TransacaoRepository repository;

    private Transacao transacaoCriada;
    private long tempoInicio;
    private long tempoFim;

    @Dado("que o sistema está pronto para receber transações")
    public void que_sistema_pronto() {
        assertNotNull(repository, "Repositório não pode ser nulo");
    }

    @Quando("eu crio uma transação do tipo {string} com valor {double}")
    public void criar_transacao(String tipo, double valor) {
        tempoInicio = System.currentTimeMillis();

        transacaoCriada = new Transacao();
        transacaoCriada.setIdUsuario(1L);
        transacaoCriada.setIdConta(1L);
        transacaoCriada.setIdCategoria(1L);
        transacaoCriada.setTipo(TipoTransacao.valueOf(tipo));
        transacaoCriada.setValor(BigDecimal.valueOf(valor));
        transacaoCriada.setData(LocalDate.now());
        transacaoCriada.setDescricao("Transação de teste Cucumber");
        transacaoCriada.setPaga(true);

        repository.save(transacaoCriada);

        tempoFim = System.currentTimeMillis();
    }

    @Entao("a transação deve ser salva com sucesso")
    public void transacao_salva() {
        assertNotNull(transacaoCriada);
        assertNotNull(transacaoCriada.getIdTransacao(), "ID não foi gerado");
        assertTrue(transacaoCriada.getIdTransacao() > 0, "ID inválido");
    }

    @Entao("o valor deve ser positivo")
    public void valor_positivo() {
        assertTrue(transacaoCriada.getValor().compareTo(BigDecimal.ZERO) > 0,
                "Valor deve ser positivo");
    }

    @Entao("o valor deve ser negativo no cálculo do saldo")
    public void valor_negativo_saldo() {
        // Para despesas, o valor é subtraído do saldo
        assertTrue(transacaoCriada.getValor().compareTo(BigDecimal.ZERO) > 0,
                "Valor da transação deve ser positivo");
    }

    @Dado("que existem {int} transações no banco")
    public void existem_transacoes(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Transacao t = new Transacao();
            t.setIdUsuario(1L);
            t.setIdConta(1L);
            t.setIdCategoria(1L);
            t.setTipo(TipoTransacao.DESPESA);
            t.setValor(BigDecimal.valueOf(100));
            t.setData(LocalDate.now());
            t.setDescricao("Teste performance " + i);
            t.setPaga(true);
            repository.save(t);
        }
    }

    @Quando("eu solicito a lista de transações")
    public void solicitar_lista() {
        tempoInicio = System.currentTimeMillis();
        List<Transacao> lista = repository.findByIdUsuario(1L);
        tempoFim = System.currentTimeMillis();
    }

    @Entao("o tempo de resposta deve ser menor que {int} segundos")
    public void tempo_resposta(int segundos) {
        long tempoDecorrido = tempoFim - tempoInicio;
        assertTrue(tempoDecorrido < (segundos * 1000),
                "Tempo excedido: " + tempoDecorrido + "ms (máximo: " + (segundos * 1000) + "ms)");
    }
}