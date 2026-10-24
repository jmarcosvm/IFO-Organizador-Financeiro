package com.ifo.backend.controller;

import com.ifo.backend.dto.ResumoResponse;
import com.ifo.backend.service.MesReferencia;
import com.ifo.backend.service.ResumoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumo")
public class ResumoController {

    private final ResumoService service;

    public ResumoController(ResumoService service) {
        this.service = service;
    }

    // GET /api/resumo?mes=10&ano=2026 (sem parâmetros: mês atual)
    @GetMapping
    public ResponseEntity<ResumoResponse> resumir(@AuthenticationPrincipal Long idUsuario,
                                                  @RequestParam(required = false) Integer mes,
                                                  @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(service.resumir(idUsuario, MesReferencia.resolver(mes, ano)));
    }
}
