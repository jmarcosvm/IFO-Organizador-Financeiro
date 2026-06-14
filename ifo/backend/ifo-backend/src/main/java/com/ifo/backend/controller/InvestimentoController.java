package com.ifo.backend.controller;

import com.ifo.backend.model.Investimento;
import com.ifo.backend.service.InvestimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/investimentos")
@CrossOrigin(origins = "*")
public class InvestimentoController {

    @Autowired
    private InvestimentoService service;

    @GetMapping
    public ResponseEntity<List<Investimento>> listar(@RequestParam Long usuario) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuario));
    }

    @PostMapping
    public ResponseEntity<Investimento> criar(@RequestBody Investimento investimento) {
        return ResponseEntity.ok(service.salvar(investimento));
    }
}