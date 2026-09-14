package com.mediqr.backend.controller;

import com.mediqr.backend.dto.ConsultaMedicaCreateRequest;
import com.mediqr.backend.dto.ConsultaMedicaUpdateRequest;
import com.mediqr.backend.model.ConsultaMedica;
import com.mediqr.backend.service.ConsultaMedicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas-medicas")
@PreAuthorize("hasRole('PERSONAL_SALUD')")
public class ConsultaMedicaController {

    private final ConsultaMedicaService consultaMedicaService;

    public ConsultaMedicaController(ConsultaMedicaService consultaMedicaService) {
        this.consultaMedicaService = consultaMedicaService;
    }

    @GetMapping
    public List<ConsultaMedica> getAll() {
        return consultaMedicaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaMedica> getById(@PathVariable Long id) {
        return ResponseEntity.ok(consultaMedicaService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ConsultaMedica> create(@Valid @RequestBody ConsultaMedicaCreateRequest request) {
        ConsultaMedica savedConsultaMedica = consultaMedicaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedConsultaMedica);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaMedica> update(@PathVariable Long id,
                                                 @Valid @RequestBody ConsultaMedicaUpdateRequest request) {
        return ResponseEntity.ok(consultaMedicaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        consultaMedicaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
