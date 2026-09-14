package com.mediqr.backend.controller;

import com.mediqr.backend.dto.AutorizacionPacienteCreateRequest;
import com.mediqr.backend.dto.AutorizacionPacienteResponse;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.AutorizacionPacienteService;
import com.mediqr.backend.service.PacienteService;
import com.mediqr.backend.service.PersonalSaludService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/autorizaciones-paciente")
public class AutorizacionPacienteController {

    private final AutorizacionPacienteService autorizacionPacienteService;
    private final CurrentUserService currentUserService;
    private final PacienteService pacienteService;
    private final PersonalSaludService personalSaludService;

    public AutorizacionPacienteController(AutorizacionPacienteService autorizacionPacienteService,
                                          CurrentUserService currentUserService,
                                          PacienteService pacienteService,
                                          PersonalSaludService personalSaludService) {
        this.autorizacionPacienteService = autorizacionPacienteService;
        this.currentUserService = currentUserService;
        this.pacienteService = pacienteService;
        this.personalSaludService = personalSaludService;
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #pacienteId)")
    public List<AutorizacionPacienteResponse> getByPacienteId(@PathVariable Long pacienteId) {
        return autorizacionPacienteService.findByPacienteId(pacienteId);
    }

    @GetMapping("/personal")
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public List<AutorizacionPacienteResponse> getByPersonal() {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        return personalSaludService.findByUsuarioId(currentUser.userId())
                .map(p -> autorizacionPacienteService.findByPersonalId(p.getId()))
                .orElse(List.of());
    }

    @PostMapping("/paciente/{pacienteId}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #pacienteId)")
    public ResponseEntity<AutorizacionPacienteResponse> createForPatient(@PathVariable Long pacienteId,
                                                                          @Valid @RequestBody AutorizacionPacienteCreateRequest request) {
        var currentUser = currentUserService.getCurrentUser();
        AutorizacionPacienteResponse response = autorizacionPacienteService.createForPatient(currentUser, pacienteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/revocar")
    public ResponseEntity<AutorizacionPacienteResponse> revoke(@PathVariable Long id) {
        var currentUser = currentUserService.getCurrentUser();
        
        // Check if user is PACIENTE
        if (!"PACIENTE".equals(currentUser.rol())) {
            return ResponseEntity.status(403).build();
        }

        // Get the autorizacion to verify ownership
        Optional<AutorizacionPacienteResponse> autorizacionOpt = autorizacionPacienteService.findById(id);
        if (autorizacionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var autorizacion = autorizacionOpt.get();

        // Verify the current user owns this patient
        Optional<com.mediqr.backend.model.Paciente> currentUserPatientOpt = pacienteService.findByUsuarioId(currentUser.userId());
        if (currentUserPatientOpt.isEmpty() || !currentUserPatientOpt.get().getId().equals(autorizacion.getPacienteId())) {
            return ResponseEntity.status(403).build();
        }

        AutorizacionPacienteResponse response = autorizacionPacienteService.revokeForPatient(currentUser, id);
        return ResponseEntity.ok(response);
    }
}
