package com.mediqr.backend.controller;

import com.mediqr.backend.authorization.domain.PatientAccessGuard;
import com.mediqr.backend.dto.MedicamentoCreateRequest;
import com.mediqr.backend.dto.MedicamentoUpdateRequest;
import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.service.MedicamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@PreAuthorize("hasRole('PERSONAL_SALUD')")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;
    private final PatientAccessGuard patientAccessGuard;

    public MedicamentoController(MedicamentoService medicamentoService,
                                 PatientAccessGuard patientAccessGuard) {
        this.medicamentoService = medicamentoService;
        this.patientAccessGuard = patientAccessGuard;
    }

    @GetMapping
    public List<Medicamento> getAll() {
        throw new AccessDeniedException("La lista global de medicamentos no está disponible");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicamento> getById(@PathVariable Long id) {
        patientAccessGuard.requirePatientAccess(medicamentoService.findPacienteIdById(id));
        return ResponseEntity.ok(medicamentoService.getById(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<Medicamento> getByPaciente(@PathVariable Long pacienteId) {
        patientAccessGuard.requirePatientAccess(pacienteId);
        return medicamentoService.findByPacienteId(pacienteId);
    }

    @PostMapping
    public ResponseEntity<Medicamento> create(@Valid @RequestBody MedicamentoCreateRequest request) {
        patientAccessGuard.requirePatientAccess(request.getPacienteId());
        Medicamento savedMedicamento = medicamentoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicamento> update(@PathVariable Long id,
                                              @Valid @RequestBody MedicamentoUpdateRequest request) {
        patientAccessGuard.requirePatientAccess(medicamentoService.findPacienteIdById(id));
        return ResponseEntity.ok(medicamentoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientAccessGuard.requirePatientAccess(medicamentoService.findPacienteIdById(id));
        medicamentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
