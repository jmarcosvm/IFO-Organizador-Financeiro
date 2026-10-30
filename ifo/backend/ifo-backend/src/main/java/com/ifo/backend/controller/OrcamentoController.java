package com.ifo.backend.controller;

import com.ifo.backend.dto.OrcamentoRequest;
import com.ifo.backend.dto.OrcamentoResponse;
import com.ifo.backend.dto.StatusGrupoResponse;
import com.ifo.backend.service.MesReferencia;
import com.ifo.backend.service.OrcamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orcamento")
public class OrcamentoController {

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoResponse>> listar(@AuthenticationPrincipal Long idUsuario) {
        return ResponseEntity.ok(service.listar(idUsuario));
    }

    @PutMapping
    public ResponseEntity<List<OrcamentoResponse>> atualizar(@AuthenticationPrincipal Long idUsuario,
                                                             @Valid @RequestBody OrcamentoRequest request) {
        return ResponseEntity.ok(service.atualizar(idUsuario, request));
    }

    // GET /api/orcamento/status?mes=10&ano=2026 (sem parâmetros: mês atual)
    @GetMapping("/status")
    public ResponseEntity<List<StatusGrupoResponse>> status(@AuthenticationPrincipal Long idUsuario,
                                                            @RequestParam(required = false) Integer mes,
                                                            @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(service.status(idUsuario, MesReferencia.resolver(mes, ano)));
    }
}
