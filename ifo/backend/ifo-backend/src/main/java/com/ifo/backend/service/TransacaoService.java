package com.ifo.backend.service;

import com.ifo.backend.model.Transacao;
import com.ifo.backend.model.TipoTransacao;
import com.ifo.backend.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository repository;

    public List<Transacao> buscarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public Transacao salvar(Transacao transacao) {
        return repository.save(transacao);
    }

    public BigDecimal calcularSaldo(Long idUsuario) {
        List<Transacao> transacoes = repository.findByIdUsuario(idUsuario);

        BigDecimal receitas = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.RECEITA)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal despesas = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.DESPESA)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return receitas.subtract(despesas);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}