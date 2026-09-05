package com.mediqr.backend.controller;

import com.mediqr.backend.dto.HistorialClinicoCreateRequest;
import com.mediqr.backend.dto.HistorialClinicoUpdateRequest;
import com.mediqr.backend.model.HistorialClinico;
import com.mediqr.backend.service.HistorialClinicoService;
import jakarta.validation.Valid;
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
        return ResponseEntity.ok(historialClinicoService.getById(id));
    }

    @PostMapping
    public ResponseEntity<HistorialClinico> create(@Valid @RequestBody HistorialClinicoCreateRequest request) {
        HistorialClinico savedHistorialClinico = historialClinicoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHistorialClinico);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialClinico> update(@PathVariable Long id,
                                                   @Valid @RequestBody HistorialClinicoUpdateRequest request) {
        return ResponseEntity.ok(historialClinicoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        historialClinicoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
