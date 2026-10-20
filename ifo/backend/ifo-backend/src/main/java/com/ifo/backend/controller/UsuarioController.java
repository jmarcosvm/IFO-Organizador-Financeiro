package com.ifo.backend.controller;

import com.ifo.backend.dto.AtualizarUsuarioRequest;
import com.ifo.backend.dto.UsuarioResponse;
import com.ifo.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> buscar(@AuthenticationPrincipal Long idUsuario) {
        return ResponseEntity.ok(service.buscar(idUsuario));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> atualizar(@AuthenticationPrincipal Long idUsuario,
                                                     @Valid @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(service.atualizar(idUsuario, request));
    }
}
