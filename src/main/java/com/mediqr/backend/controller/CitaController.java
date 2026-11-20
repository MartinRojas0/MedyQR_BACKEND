package com.mediqr.backend.controller;

import com.mediqr.backend.authorization.domain.PatientAccessGuard;
import com.mediqr.backend.dto.CitaCreateRequest;
import com.mediqr.backend.dto.CitaEstadoRequest;
import com.mediqr.backend.dto.CitaUpdateRequest;
import com.mediqr.backend.model.Cita;
import com.mediqr.backend.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@PreAuthorize("hasRole('PERSONAL_SALUD')")
public class CitaController {

    private final CitaService citaService;
    private final PatientAccessGuard patientAccessGuard;

    public CitaController(CitaService citaService, PatientAccessGuard patientAccessGuard) {
        this.citaService = citaService;
        this.patientAccessGuard = patientAccessGuard;
    }

    @GetMapping
    public List<Cita> getAll() {
        throw new AccessDeniedException("La lista global de citas no está disponible");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getById(@PathVariable Long id) {
        patientAccessGuard.requirePatientAccess(citaService.findPacienteIdById(id));
        return ResponseEntity.ok(citaService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Cita> create(@Valid @RequestBody CitaCreateRequest request) {
        patientAccessGuard.requirePatientAccess(request.getPacienteId());
        Cita savedCita = citaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCita);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> update(@PathVariable Long id, @Valid @RequestBody CitaUpdateRequest request) {
        patientAccessGuard.requirePatientAccess(citaService.findPacienteIdById(id));
        return ResponseEntity.ok(citaService.update(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Cita> updateEstado(@PathVariable Long id,
                                             @Valid @RequestBody CitaEstadoRequest request) {
        patientAccessGuard.requirePatientAccess(citaService.findPacienteIdById(id));
        return ResponseEntity.ok(citaService.updateEstado(id, request));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> disponibilidad(
            @RequestParam Long personalId,
            @RequestParam OffsetDateTime fechaHora) {
        return ResponseEntity.ok(new DisponibilidadResponse(
                citaService.isAvailable(personalId, fechaHora)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientAccessGuard.requirePatientAccess(citaService.findPacienteIdById(id));
        citaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record DisponibilidadResponse(boolean disponible) {
    }
}
