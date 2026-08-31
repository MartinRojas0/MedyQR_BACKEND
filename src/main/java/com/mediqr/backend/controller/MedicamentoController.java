package com.mediqr.backend.controller;

import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.service.MedicamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    public MedicamentoController(MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
    }

    @GetMapping
    public List<Medicamento> getAll() {
        return medicamentoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicamento> getById(@PathVariable Long id) {
        return medicamentoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Medicamento> create(@RequestBody Medicamento medicamento) {
        Medicamento savedMedicamento = medicamentoService.save(medicamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicamento> update(@PathVariable Long id, @RequestBody Medicamento medicamento) {
        return medicamentoService.update(id, medicamento)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (medicamentoService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        medicamentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
