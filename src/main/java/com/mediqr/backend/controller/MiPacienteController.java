package com.mediqr.backend.controller;

import com.mediqr.backend.dto.PacienteUpdateRequest;
import com.mediqr.backend.dto.EmergencyContactResponse;
import com.mediqr.backend.dto.EmergencyContactUpdateRequest;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
@PreAuthorize("hasRole('PACIENTE')")
public class MiPacienteController {

    private final PacienteService pacienteService;
    private final CurrentUserService currentUserService;

    public MiPacienteController(PacienteService pacienteService, CurrentUserService currentUserService) {
        this.pacienteService = pacienteService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<Paciente> getMe() {
        Long usuarioId = currentUserService.getCurrentUserId();
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.of(pacienteService.findByUsuarioId(usuarioId));
    }

    @PutMapping("/me")
    public ResponseEntity<Paciente> updateMe(@Valid @RequestBody PacienteUpdateRequest request) {
        Long usuarioId = currentUserService.getCurrentUserId();
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }
        return pacienteService.findByUsuarioId(usuarioId)
                .map(paciente -> ResponseEntity.ok(pacienteService.update(paciente.getId(), request)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me/contacto-emergencia")
    public ResponseEntity<EmergencyContactResponse> getEmergencyContactMe() {
        Long usuarioId = currentUserService.getCurrentUserId();
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }
        return pacienteService.findByUsuarioId(usuarioId)
                .map(paciente -> ResponseEntity.ok(new EmergencyContactResponse(
                        paciente.getContactoEmergenciaNombre(),
                        paciente.getContactoEmergenciaParentesco(),
                        paciente.getContactoEmergenciaTelefono())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/me/contacto-emergencia")
    public ResponseEntity<EmergencyContactResponse> updateEmergencyContactMe(
            @Valid @RequestBody EmergencyContactUpdateRequest request) {
        Long usuarioId = currentUserService.getCurrentUserId();
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }
        Paciente paciente = pacienteService.updateEmergencyContact(usuarioId, request);
        return ResponseEntity.ok(new EmergencyContactResponse(
                paciente.getContactoEmergenciaNombre(),
                paciente.getContactoEmergenciaParentesco(),
                paciente.getContactoEmergenciaTelefono()));
    }

    @DeleteMapping("/me/contacto-emergencia")
    public ResponseEntity<Void> clearEmergencyContactMe() {
        Long usuarioId = currentUserService.getCurrentUserId();
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }
        pacienteService.clearEmergencyContact(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
