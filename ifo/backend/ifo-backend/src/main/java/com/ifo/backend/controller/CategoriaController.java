package com.ifo.backend.controller;

import com.ifo.backend.model.Categoria;
import com.ifo.backend.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaService service;

    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        List<Categoria> categorias = service.buscarTodas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Categoria> criar(@RequestBody Categoria categoria) {
        Categoria salva = service.salvar(categoria);
        return ResponseEntity.ok(salva);
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

    @PostMapping("/inicializar")
    public ResponseEntity<String> inicializarPadrao() {
        service.inicializarCategoriasPadrao();
        return ResponseEntity.ok("Categorias padrão inicializadas com sucesso!");
    }
}