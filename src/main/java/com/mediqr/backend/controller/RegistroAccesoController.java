package com.mediqr.backend.controller;

import com.mediqr.backend.model.RegistroAcceso;
import com.mediqr.backend.service.RegistroAccesoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registro-accesos")
public class RegistroAccesoController {

    private final RegistroAccesoService registroAccesoService;

    public RegistroAccesoController(RegistroAccesoService registroAccesoService) {
        this.registroAccesoService = registroAccesoService;
    }

    @GetMapping
    public List<RegistroAcceso> getAll() {
        return registroAccesoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroAcceso> getById(@PathVariable Long id) {
        return registroAccesoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RegistroAcceso> create(@RequestBody RegistroAcceso registroAcceso) {
        RegistroAcceso savedRegistroAcceso = registroAccesoService.save(registroAcceso);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRegistroAcceso);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistroAcceso> update(@PathVariable Long id, @RequestBody RegistroAcceso registroAcceso) {
        return registroAccesoService.update(id, registroAcceso)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (registroAccesoService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        registroAccesoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
