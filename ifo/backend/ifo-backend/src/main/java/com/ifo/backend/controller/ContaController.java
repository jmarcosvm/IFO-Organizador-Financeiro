package com.ifo.backend.controller;

import com.ifo.backend.model.ContaCarteira;
import com.ifo.backend.service.ContaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contas")
@CrossOrigin(origins = "*")
public class ContaController {

    @Autowired
    private ContaService service;

    @GetMapping
    public ResponseEntity<List<ContaCarteira>> listar(@RequestParam Long usuario) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuario));
    }

    @PostMapping
    public ResponseEntity<ContaCarteira> criar(@RequestBody ContaCarteira conta) {
        return ResponseEntity.ok(service.salvar(conta));
    }
}