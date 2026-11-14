package com.mediqr.backend.controller;

import com.mediqr.backend.dto.HistorialClinicoUpdateRequest;
import com.mediqr.backend.model.HistorialClinico;
import jakarta.validation.Valid;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.HistorialClinicoService;
import com.mediqr.backend.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historial-clinico")
@PreAuthorize("hasRole('PACIENTE')")
public class MiHistorialClinicoController {

    private final PacienteService pacienteService;
    private final HistorialClinicoService historialClinicoService;
    private final CurrentUserService currentUserService;

    public MiHistorialClinicoController(PacienteService pacienteService,
                                        HistorialClinicoService historialClinicoService,
                                        CurrentUserService currentUserService) {
        this.pacienteService = pacienteService;
        this.historialClinicoService = historialClinicoService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<HistorialClinico> getMe() {
        Long usuarioId = currentUserService.getCurrentUserId();
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }

        return pacienteService.findByUsuarioId(usuarioId)
                .flatMap(paciente -> historialClinicoService.findByPacienteId(paciente.getId()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<HistorialClinico> updateMe(
            @Valid @RequestBody HistorialClinicoUpdateRequest request) {
        Long usuarioId = currentUserService.getCurrentUserId();
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }

        return pacienteService.findByUsuarioId(usuarioId)
                .map(paciente -> ResponseEntity.ok(
                        historialClinicoService.upsertForPaciente(paciente.getId(), request)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
