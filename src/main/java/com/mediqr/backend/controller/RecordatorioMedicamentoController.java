package com.mediqr.backend.controller;

import com.mediqr.backend.dto.RecordatorioMedicamentoCreateRequest;
import com.mediqr.backend.dto.RecordatorioMedicamentoUpdateRequest;
import com.mediqr.backend.model.RecordatorioMedicamento;
import com.mediqr.backend.service.RecordatorioMedicamentoService;
import jakarta.validation.Valid;
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
        return ResponseEntity.ok(recordatorioMedicamentoService.getById(id));
    }

    @GetMapping("/medicamento/{medicamentoId}")
    public List<RecordatorioMedicamento> getByMedicamento(@PathVariable Long medicamentoId) {
        return recordatorioMedicamentoService.findByMedicamentoId(medicamentoId);
    }

    @PostMapping
    public ResponseEntity<RecordatorioMedicamento> create(
            @Valid @RequestBody RecordatorioMedicamentoCreateRequest request) {
        RecordatorioMedicamento savedRecordatorioMedicamento = recordatorioMedicamentoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecordatorioMedicamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordatorioMedicamento> update(
            @PathVariable Long id, @Valid @RequestBody RecordatorioMedicamentoUpdateRequest request) {
        return ResponseEntity.ok(recordatorioMedicamentoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recordatorioMedicamentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
