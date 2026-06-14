package com.ifo.backend.controller;

import com.ifo.backend.model.Transacao;
import com.ifo.backend.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@CrossOrigin(origins = "*")
public class TransacaoController {

    @Autowired
    private TransacaoService service;

    @GetMapping
    public ResponseEntity<List<Transacao>> listar(@RequestParam Long usuario) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuario));
    }

    @GetMapping("/saldo")
    public ResponseEntity<BigDecimal> calcularSaldo(@RequestParam Long usuario) {
        return ResponseEntity.ok(service.calcularSaldo(usuario));
    }

    @PostMapping
    public ResponseEntity<Transacao> criar(@RequestBody Transacao transacao) {
        return ResponseEntity.ok(service.salvar(transacao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.ok().build();
    }
}