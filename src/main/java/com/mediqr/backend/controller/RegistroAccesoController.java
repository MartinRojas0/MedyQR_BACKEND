package com.mediqr.backend.controller;

import com.mediqr.backend.model.RegistroAcceso;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import com.mediqr.backend.service.PacienteService;
import com.mediqr.backend.service.PersonalSaludService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/registro-accesos")
public class RegistroAccesoController {

    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;
    private final PacienteService pacienteService;
    private final PersonalSaludService personalSaludService;

    public RegistroAccesoController(RegistroAccesoService registroAccesoService,
                                    CurrentUserService currentUserService,
                                    PacienteService pacienteService,
                                    PersonalSaludService personalSaludService) {
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
        this.pacienteService = pacienteService;
        this.personalSaludService = personalSaludService;
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #pacienteId)")
    public List<RegistroAcceso> getByPacienteId(@PathVariable Long pacienteId) {
        return registroAccesoService.findByPacienteId(pacienteId);
    }

    @GetMapping("/personal")
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public List<RegistroAcceso> getByPersonal() {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        return personalSaludService.findByUsuarioId(currentUser.userId())
                .map(p -> registroAccesoService.findByPersonalId(p.getId()))
                .orElse(List.of());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #registro.pacienteId)")
    public ResponseEntity<RegistroAcceso> getById(@PathVariable Long id) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<RegistroAcceso> registroOpt = registroAccesoService.findById(id);
        if (registroOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RegistroAcceso registro = registroOpt.get();

        // Check authorization based on role
        if ("PACIENTE".equals(currentUser.rol())) {
            Optional<com.mediqr.backend.model.Paciente> pacienteOpt = pacienteService.findByUsuarioId(currentUser.userId());
            if (pacienteOpt.isEmpty() || !pacienteOpt.get().getId().equals(registro.getPacienteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if ("PERSONAL_SALUD".equals(currentUser.rol())) {
            Optional<com.mediqr.backend.model.PersonalSalud> personalOpt = personalSaludService.findByUsuarioId(currentUser.userId());
            if (personalOpt.isEmpty() || !personalOpt.get().getId().equals(registro.getPersonalId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(registro);
    }
}
