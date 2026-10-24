package com.ifo.backend.service;

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

    public TransacaoService(TransacaoRepository repository, UsuarioRepository usuarioRepository,
                            CategoriaService categoriaService) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar(Long idUsuario, YearMonth mes) {
        return repository.findByUsuarioIdUsuarioAndDataBetweenOrderByDataDescIdTransacaoDesc(
                        idUsuario, mes.atDay(1), mes.atEndOfMonth()).stream()
                .map(TransacaoResponse::from)
                .toList();
    }

    @Transactional
    public TransacaoResponse criar(Long idUsuario, TransacaoRequest request) {
        Transacao transacao = new Transacao();
        transacao.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        transacao.setCategoria(categoriaService.buscarEntidade(request.idCategoria()));
        transacao.setValor(request.valor());
        transacao.setData(request.data());
        transacao.setDescricao(request.descricao());
        return TransacaoResponse.from(repository.save(transacao));
    }

    @Transactional
    public void excluir(Long idUsuario, Long idTransacao) {
        Transacao transacao = repository.findByIdTransacaoAndUsuarioIdUsuario(idTransacao, idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada."));
        repository.delete(transacao);
    }
}
