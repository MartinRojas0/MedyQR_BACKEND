package com.mediqr.backend.controller;

import com.mediqr.backend.model.ConsultaMedica;
import com.mediqr.backend.service.ConsultaMedicaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas-medicas")
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
        return consultaMedicaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ConsultaMedica> create(@RequestBody ConsultaMedica consultaMedica) {
        ConsultaMedica savedConsultaMedica = consultaMedicaService.save(consultaMedica);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedConsultaMedica);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaMedica> update(@PathVariable Long id, @RequestBody ConsultaMedica consultaMedica) {
        return consultaMedicaService.update(id, consultaMedica)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (consultaMedicaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        consultaMedicaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
