package com.mediqr.backend.controller;

import com.mediqr.backend.model.HistorialClinico;
import com.mediqr.backend.service.HistorialClinicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial-clinico")
public class HistorialClinicoController {

    private final HistorialClinicoService historialClinicoService;

    public HistorialClinicoController(HistorialClinicoService historialClinicoService) {
        this.historialClinicoService = historialClinicoService;
    }

    @GetMapping
    public List<HistorialClinico> getAll() {
        return historialClinicoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialClinico> getById(@PathVariable Long id) {
        return historialClinicoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HistorialClinico> create(@RequestBody HistorialClinico historialClinico) {
        HistorialClinico savedHistorialClinico = historialClinicoService.save(historialClinico);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHistorialClinico);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialClinico> update(@PathVariable Long id, @RequestBody HistorialClinico historialClinico) {
        return historialClinicoService.update(id, historialClinico)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (historialClinicoService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        historialClinicoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
