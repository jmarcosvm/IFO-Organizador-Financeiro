package com.ifo.backend.controller;

import com.ifo.backend.dto.OrcamentoResponse;
import com.ifo.backend.service.OrcamentoService;
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
}
