package com.mediqr.backend.controller;

import com.mediqr.backend.dto.UsuarioCreateRequest;
import com.mediqr.backend.dto.UsuarioResponse;
import com.mediqr.backend.dto.UsuarioUpdateRequest;
import com.mediqr.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> getAll() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> getById(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse saved = usuarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> update(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (usuarioService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
