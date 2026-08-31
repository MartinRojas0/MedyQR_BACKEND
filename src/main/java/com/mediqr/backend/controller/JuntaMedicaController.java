package com.mediqr.backend.controller;

import com.mediqr.backend.model.JuntaMedica;
import com.mediqr.backend.service.JuntaMedicaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/juntas-medicas")
public class JuntaMedicaController {

    private final JuntaMedicaService juntaMedicaService;

    public JuntaMedicaController(JuntaMedicaService juntaMedicaService) {
        this.juntaMedicaService = juntaMedicaService;
    }

    @GetMapping
    public List<JuntaMedica> getAll() {
        return juntaMedicaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JuntaMedica> getById(@PathVariable Long id) {
        return juntaMedicaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<JuntaMedica> create(@RequestBody JuntaMedica juntaMedica) {
        JuntaMedica savedJuntaMedica = juntaMedicaService.save(juntaMedica);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedJuntaMedica);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JuntaMedica> update(@PathVariable Long id, @RequestBody JuntaMedica juntaMedica) {
        return juntaMedicaService.update(id, juntaMedica)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (juntaMedicaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        juntaMedicaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
