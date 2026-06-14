package com.ifo.backend.controller;

import com.ifo.backend.model.MetaFinanceira;
import com.ifo.backend.service.MetaFinanceiraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/metas")
@CrossOrigin(origins = "*")
public class MetaFinanceiraController {

    @Autowired
    private MetaFinanceiraService service;

    @GetMapping
    public ResponseEntity<List<MetaFinanceira>> buscarPorUsuario(@RequestParam Long usuario) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuario));
    }

    @PostMapping
    public ResponseEntity<MetaFinanceira> criar(@RequestBody MetaFinanceira meta) {
        return ResponseEntity.ok(service.salvar(meta));
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