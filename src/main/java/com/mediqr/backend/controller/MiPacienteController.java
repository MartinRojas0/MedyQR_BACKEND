package com.mediqr.backend.controller;

import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
}
