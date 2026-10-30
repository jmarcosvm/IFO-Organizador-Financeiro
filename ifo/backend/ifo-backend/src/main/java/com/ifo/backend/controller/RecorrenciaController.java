package com.ifo.backend.controller;

import com.ifo.backend.dto.AtivaRequest;
import com.ifo.backend.dto.RecorrenciaRequest;
import com.ifo.backend.dto.RecorrenciaResponse;
import com.ifo.backend.service.RecorrenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recorrencias")
public class RecorrenciaController {

    private final RecorrenciaService service;

    public RecorrenciaController(RecorrenciaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RecorrenciaResponse>> listar(@AuthenticationPrincipal Long idUsuario) {
        return ResponseEntity.ok(service.listar(idUsuario));
    }

    @PostMapping
    public ResponseEntity<RecorrenciaResponse> criar(@AuthenticationPrincipal Long idUsuario,
                                                     @Valid @RequestBody RecorrenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(idUsuario, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecorrenciaResponse> atualizar(@AuthenticationPrincipal Long idUsuario, @PathVariable Long id,
                                                         @Valid @RequestBody RecorrenciaRequest request) {
        return ResponseEntity.ok(service.atualizar(idUsuario, id, request));
    }

    // PATCH /api/recorrencias/{id}/ativa com { "ativa": false } pausa; true reativa
    @PatchMapping("/{id}/ativa")
    public ResponseEntity<RecorrenciaResponse> definirAtiva(@AuthenticationPrincipal Long idUsuario, @PathVariable Long id,
                                                            @Valid @RequestBody AtivaRequest request) {
        return ResponseEntity.ok(service.definirAtiva(idUsuario, id, request.ativa()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Long idUsuario, @PathVariable Long id) {
        service.excluir(idUsuario, id);
        return ResponseEntity.noContent().build();
    }
}
