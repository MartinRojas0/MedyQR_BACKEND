package com.mediqr.backend.controller;

import com.mediqr.backend.dto.MedicamentoCreateRequest;
import com.mediqr.backend.dto.MedicamentoUpdateRequest;
import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.service.MedicamentoService;
import jakarta.validation.Valid;
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
        return ResponseEntity.ok(medicamentoService.getById(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<Medicamento> getByPaciente(@PathVariable Long pacienteId) {
        return medicamentoService.findByPacienteId(pacienteId);
    }

    @PostMapping
    public ResponseEntity<Medicamento> create(@Valid @RequestBody MedicamentoCreateRequest request) {
        Medicamento savedMedicamento = medicamentoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicamento> update(@PathVariable Long id,
                                              @Valid @RequestBody MedicamentoUpdateRequest request) {
        return ResponseEntity.ok(medicamentoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicamentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
