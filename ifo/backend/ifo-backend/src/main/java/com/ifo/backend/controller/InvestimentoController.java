package com.ifo.backend.controller;

import com.ifo.backend.dto.SimulacaoRequest;
import com.ifo.backend.dto.SimulacaoResponse;
import com.ifo.backend.service.SimuladorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investimentos")
public class InvestimentoController {

    private final SimuladorService service;

    public InvestimentoController(SimuladorService service) {
        this.service = service;
    }

    // Só calcula e devolve; não grava nada e não depende do usuário
    @PostMapping("/simular")
    public ResponseEntity<SimulacaoResponse> simular(@Valid @RequestBody SimulacaoRequest request) {
        return ResponseEntity.ok(service.simular(request));
    }
}
