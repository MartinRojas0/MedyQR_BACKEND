package com.mediqr.backend.controller;

import com.mediqr.backend.dto.PacienteCreateRequest;
import com.mediqr.backend.dto.PacienteUpdateRequest;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public List<Paciente> getAll() {
        return pacienteService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paciente> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Paciente> create(@Valid @RequestBody PacienteCreateRequest request) {
        Paciente savedPaciente = pacienteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPaciente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> update(@PathVariable Long id,
                                           @Valid @RequestBody PacienteUpdateRequest request) {
        return ResponseEntity.ok(pacienteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pacienteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
