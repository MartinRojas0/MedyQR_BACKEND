package com.mediqr.backend.controller;

import com.mediqr.backend.authorization.domain.PatientAccessGuard;
import com.mediqr.backend.dto.RecordatorioMedicamentoCreateRequest;
import com.mediqr.backend.dto.RecordatorioMedicamentoUpdateRequest;
import com.mediqr.backend.model.RecordatorioMedicamento;
import com.mediqr.backend.service.RecordatorioMedicamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recordatorios-medicamentos")
@PreAuthorize("hasRole('PERSONAL_SALUD')")
public class RecordatorioMedicamentoController {

    private final RecordatorioMedicamentoService recordatorioMedicamentoService;
    private final PatientAccessGuard patientAccessGuard;

    public RecordatorioMedicamentoController(RecordatorioMedicamentoService recordatorioMedicamentoService,
                                             PatientAccessGuard patientAccessGuard) {
        this.recordatorioMedicamentoService = recordatorioMedicamentoService;
        this.patientAccessGuard = patientAccessGuard;
    }

    @GetMapping
    public List<RecordatorioMedicamento> getAll() {
        throw new AccessDeniedException("La lista global de recordatorios no está disponible");
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecordatorioMedicamento> getById(@PathVariable Long id) {
        patientAccessGuard.requirePatientAccess(recordatorioMedicamentoService.findPacienteIdById(id));
        return ResponseEntity.ok(recordatorioMedicamentoService.getById(id));
    }

    @GetMapping("/medicamento/{medicamentoId}")
    public List<RecordatorioMedicamento> getByMedicamento(@PathVariable Long medicamentoId) {
        patientAccessGuard.requirePatientAccess(
                recordatorioMedicamentoService.findPacienteIdByMedicamentoId(medicamentoId));
        return recordatorioMedicamentoService.findByMedicamentoId(medicamentoId);
    }

    @PostMapping
    public ResponseEntity<RecordatorioMedicamento> create(
            @Valid @RequestBody RecordatorioMedicamentoCreateRequest request) {
        patientAccessGuard.requirePatientAccess(
                recordatorioMedicamentoService.findPacienteIdByMedicamentoId(request.getMedicamentoId()));
        RecordatorioMedicamento savedRecordatorioMedicamento = recordatorioMedicamentoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecordatorioMedicamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordatorioMedicamento> update(
            @PathVariable Long id, @Valid @RequestBody RecordatorioMedicamentoUpdateRequest request) {
        patientAccessGuard.requirePatientAccess(recordatorioMedicamentoService.findPacienteIdById(id));
        return ResponseEntity.ok(recordatorioMedicamentoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientAccessGuard.requirePatientAccess(recordatorioMedicamentoService.findPacienteIdById(id));
        recordatorioMedicamentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
