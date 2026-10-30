package com.ifo.backend.service;

import com.ifo.backend.dto.AlertaOrcamentoResponse;
import com.ifo.backend.dto.TransacaoComAlertaResponse;
import com.ifo.backend.dto.TransacaoRequest;
import com.ifo.backend.dto.TransacaoResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.model.Transacao;
import com.ifo.backend.repository.TransacaoRepository;
import com.ifo.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.YearMonth;
import java.util.List;

// Toda operação filtra pelo usuário do token (RN11)
@Service
public class TransacaoService {

    private final TransacaoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final OrcamentoService orcamentoService;

    public TransacaoService(TransacaoRepository repository, UsuarioRepository usuarioRepository,
                            CategoriaService categoriaService, OrcamentoService orcamentoService) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
        this.orcamentoService = orcamentoService;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar(Long idUsuario, YearMonth mes) {
        return repository.findByUsuarioIdUsuarioAndDataBetweenOrderByDataDescIdTransacaoDesc(
                        idUsuario, mes.atDay(1), mes.atEndOfMonth()).stream()
                .map(TransacaoResponse::from)
                .toList();
    }

    @Transactional
    public TransacaoComAlertaResponse criar(Long idUsuario, TransacaoRequest request) {
        Transacao transacao = new Transacao();
        transacao.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        transacao.setCategoria(categoriaService.buscarEntidade(request.idCategoria()));
        transacao.setValor(request.valor());
        transacao.setData(request.data());
        transacao.setDescricao(request.descricao());
        return comAlerta(idUsuario, repository.save(transacao));
    }

    // Edita no lugar, mantendo o mesmo id (na V1 era "apagar e recriar")
    @Transactional
    public TransacaoComAlertaResponse atualizar(Long idUsuario, Long idTransacao, TransacaoRequest request) {
        Transacao transacao = buscarDoUsuario(idUsuario, idTransacao);
        transacao.setCategoria(categoriaService.buscarEntidade(request.idCategoria()));
        transacao.setValor(request.valor());
        transacao.setData(request.data());
        transacao.setDescricao(request.descricao());
        return comAlerta(idUsuario, transacao);
    }

    @Transactional
    public void excluir(Long idUsuario, Long idTransacao) {
        repository.delete(buscarDoUsuario(idUsuario, idTransacao));
    }

    // O alerta é só informativo (RN08): a transação já foi salva, nada aqui a bloqueia
    private TransacaoComAlertaResponse comAlerta(Long idUsuario, Transacao transacao) {
        AlertaOrcamentoResponse alerta = orcamentoService.alertar(idUsuario,
                transacao.getCategoria().getGrupo(), YearMonth.from(transacao.getData()));
        return new TransacaoComAlertaResponse(TransacaoResponse.from(transacao), alerta);
    }

    // Transação de outro usuário responde igual a inexistente (404)
    private Transacao buscarDoUsuario(Long idUsuario, Long idTransacao) {
        return repository.findByIdTransacaoAndUsuarioIdUsuario(idTransacao, idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada."));
    }
}
