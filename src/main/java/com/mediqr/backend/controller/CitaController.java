package com.mediqr.backend.controller;

import com.mediqr.backend.model.Cita;
import com.mediqr.backend.service.CitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public List<Cita> getAll() {
        return citaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getById(@PathVariable Long id) {
        return citaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cita> create(@RequestBody Cita cita) {
        Cita savedCita = citaService.save(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCita);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> update(@PathVariable Long id, @RequestBody Cita cita) {
        return citaService.update(id, cita)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (citaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        citaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
