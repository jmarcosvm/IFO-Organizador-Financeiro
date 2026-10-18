package com.ifo.backend.controller;

import com.ifo.backend.dto.MetaRequest;
import com.ifo.backend.dto.MetaResponse;
import com.ifo.backend.service.MetaFinanceiraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class MetaFinanceiraController {

    private final MetaFinanceiraService service;

    public MetaFinanceiraController(MetaFinanceiraService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<MetaResponse>> listar(@AuthenticationPrincipal Long idUsuario) {
        return ResponseEntity.ok(service.listar(idUsuario));
    }

    @PostMapping
    public ResponseEntity<MetaResponse> criar(@AuthenticationPrincipal Long idUsuario,
                                              @Valid @RequestBody MetaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(idUsuario, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Long idUsuario, @PathVariable Long id) {
        service.excluir(idUsuario, id);
        return ResponseEntity.noContent().build();
    }
}
