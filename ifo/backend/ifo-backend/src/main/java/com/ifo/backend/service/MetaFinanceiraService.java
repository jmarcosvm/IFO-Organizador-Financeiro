package com.ifo.backend.service;

import com.ifo.backend.dto.AporteRequest;
import com.ifo.backend.dto.MetaRequest;
import com.ifo.backend.dto.MetaResponse;
import com.ifo.backend.exception.RecursoNaoEncontradoException;
import com.ifo.backend.exception.RegraNegocioException;
import com.ifo.backend.model.MetaFinanceira;
import com.ifo.backend.repository.MetaFinanceiraRepository;
import com.ifo.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

// Metas e aportes (RF15–RF17, RN09); toda operação filtra pelo usuário do token (RN11)
@Service
public class MetaFinanceiraService {

    // Maior valor que cabe em NUMERIC(12,2)
    private static final BigDecimal LIMITE_VALOR = new BigDecimal("9999999999.99");

    private final MetaFinanceiraRepository repository;
    private final UsuarioRepository usuarioRepository;

    public MetaFinanceiraService(MetaFinanceiraRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<MetaResponse> listar(Long idUsuario) {
        return repository.findByUsuarioIdUsuarioOrderByIdMetaAsc(idUsuario).stream()
                .map(MetaResponse::from)
                .toList();
    }

    @Transactional
    public MetaResponse criar(Long idUsuario, MetaRequest request) {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        meta.setTituloMeta(request.tituloMeta().trim());
        meta.setValorObjetivo(request.valorObjetivo());
        meta.setPrazo(request.prazo());
        return MetaResponse.from(repository.save(meta));
    }

    // Edita título, objetivo e prazo; o valor já guardado não muda (só os aportes mexem nele)
    @Transactional
    public MetaResponse atualizar(Long idUsuario, Long idMeta, MetaRequest request) {
        MetaFinanceira meta = buscarDoUsuario(idUsuario, idMeta);
        meta.setTituloMeta(request.tituloMeta().trim());
        meta.setValorObjetivo(request.valorObjetivo());
        meta.setPrazo(request.prazo());
        return MetaResponse.from(meta);
    }

    // Soma o aporte ao valor atual; passar do objetivo é permitido (a meta só fica concluída)
    @Transactional
    public MetaResponse aportar(Long idUsuario, Long idMeta, AporteRequest request) {
        MetaFinanceira meta = buscarDoUsuario(idUsuario, idMeta);
        BigDecimal novoValor = meta.getValorAtual().add(request.valor());
        if (novoValor.compareTo(LIMITE_VALOR) > 0) {
            throw new RegraNegocioException("O valor guardado na meta excede o limite permitido.");
        }
        meta.setValorAtual(novoValor);
        return MetaResponse.from(meta);
    }

    @Transactional
    public void excluir(Long idUsuario, Long idMeta) {
        repository.delete(buscarDoUsuario(idUsuario, idMeta));
    }

    // Meta de outro usuário responde igual a inexistente (404)
    private MetaFinanceira buscarDoUsuario(Long idUsuario, Long idMeta) {
        return repository.findByIdMetaAndUsuarioIdUsuario(idMeta, idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Meta não encontrada."));
    }
}
