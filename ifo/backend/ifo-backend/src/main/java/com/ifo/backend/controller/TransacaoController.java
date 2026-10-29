package com.ifo.backend.controller;

import com.ifo.backend.dto.TransacaoComAlertaResponse;
import com.ifo.backend.dto.TransacaoRequest;
import com.ifo.backend.dto.TransacaoResponse;
import com.ifo.backend.service.MesReferencia;
import com.ifo.backend.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    // GET /api/transacoes?mes=10&ano=2026 (sem parâmetros: mês atual)
    @GetMapping
    public ResponseEntity<List<TransacaoResponse>> listar(@AuthenticationPrincipal Long idUsuario,
                                                          @RequestParam(required = false) Integer mes,
                                                          @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(service.listar(idUsuario, MesReferencia.resolver(mes, ano)));
    }

    @PostMapping
    public ResponseEntity<TransacaoComAlertaResponse> criar(@AuthenticationPrincipal Long idUsuario,
                                                          @Valid @RequestBody TransacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(idUsuario, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransacaoComAlertaResponse> atualizar(@AuthenticationPrincipal Long idUsuario, @PathVariable Long id,
                                                                  @Valid @RequestBody TransacaoRequest request) {
        return ResponseEntity.ok(service.atualizar(idUsuario, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Long idUsuario, @PathVariable Long id) {
        service.excluir(idUsuario, id);
        return ResponseEntity.noContent().build();
    }
}
