package com.mediqr.backend.controller;

import com.mediqr.backend.model.RecordatorioMedicamento;
import com.mediqr.backend.service.RecordatorioMedicamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recordatorios-medicamentos")
public class RecordatorioMedicamentoController {

    private final RecordatorioMedicamentoService recordatorioMedicamentoService;

    public RecordatorioMedicamentoController(RecordatorioMedicamentoService recordatorioMedicamentoService) {
        this.recordatorioMedicamentoService = recordatorioMedicamentoService;
    }

    @GetMapping
    public List<RecordatorioMedicamento> getAll() {
        return recordatorioMedicamentoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecordatorioMedicamento> getById(@PathVariable Long id) {
        return recordatorioMedicamentoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RecordatorioMedicamento> create(@RequestBody RecordatorioMedicamento recordatorioMedicamento) {
        RecordatorioMedicamento savedRecordatorioMedicamento = recordatorioMedicamentoService.save(recordatorioMedicamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecordatorioMedicamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordatorioMedicamento> update(@PathVariable Long id, @RequestBody RecordatorioMedicamento recordatorioMedicamento) {
        return recordatorioMedicamentoService.update(id, recordatorioMedicamento)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (recordatorioMedicamentoService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        recordatorioMedicamentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
