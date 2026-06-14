package com.ifo.backend.controller;

import com.ifo.backend.model.OrcamentoMensal;
import com.ifo.backend.service.OrcamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@CrossOrigin(origins = "*")
public class OrcamentoController {

    @Autowired
    private OrcamentoService service;

    @GetMapping
    public ResponseEntity<List<OrcamentoMensal>> buscarPorUsuario(@RequestParam Long usuario) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuario));
    }

    @PostMapping
    public ResponseEntity<OrcamentoMensal> criar(@RequestBody OrcamentoMensal orcamento) {
        return ResponseEntity.ok(service.salvar(orcamento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            service.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}