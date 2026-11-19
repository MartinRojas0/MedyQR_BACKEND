package com.mediqr.backend.controller;

import com.mediqr.backend.model.AutorizacionPaciente;
import com.mediqr.backend.service.AutorizacionPacienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autorizaciones-paciente")
public class AutorizacionPacienteController {

    private final AutorizacionPacienteService autorizacionPacienteService;

    public AutorizacionPacienteController(AutorizacionPacienteService autorizacionPacienteService) {
        this.autorizacionPacienteService = autorizacionPacienteService;
    }

    @GetMapping
    public List<AutorizacionPaciente> getAll() {
        return autorizacionPacienteService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutorizacionPaciente> getById(@PathVariable Long id) {
        return autorizacionPacienteService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AutorizacionPaciente> create(@RequestBody AutorizacionPaciente autorizacionPaciente) {
        AutorizacionPaciente savedAutorizacionPaciente = autorizacionPacienteService.save(autorizacionPaciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAutorizacionPaciente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AutorizacionPaciente> update(@PathVariable Long id, @RequestBody AutorizacionPaciente autorizacionPaciente) {
        return autorizacionPacienteService.update(id, autorizacionPaciente)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (autorizacionPacienteService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        autorizacionPacienteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
