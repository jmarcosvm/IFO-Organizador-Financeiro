package com.ifo.backend.controller;

import com.ifo.backend.model.Usuario;
import com.ifo.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(service.salvar(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario loginData) {
        Usuario usuario = service.login(loginData.getEmail(), loginData.getSenha());
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.status(401).build();
    }
}