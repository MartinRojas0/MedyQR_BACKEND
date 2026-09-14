package com.mediqr.backend.controller;

import com.mediqr.backend.dto.ParticipanteJuntaCreateRequest;
import com.mediqr.backend.dto.ParticipanteJuntaResponse;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.service.ParticipanteJuntaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participantes-junta")
public class ParticipanteJuntaController {

    private final ParticipanteJuntaService participanteJuntaService;

    public ParticipanteJuntaController(ParticipanteJuntaService participanteJuntaService) {
        this.participanteJuntaService = participanteJuntaService;
    }

    @GetMapping("/junta/{juntaId}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #junta.pacienteId)")
    public List<ParticipanteJuntaResponse> getByJuntaId(@PathVariable Long juntaId) {
        return participanteJuntaService.findByJuntaId(juntaId);
    }

    @GetMapping("/personal")
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public List<ParticipanteJuntaResponse> getByPersonal() {
        // This would need the service to support finding by personal
        return List.of();
    }

    @PostMapping("/junta/{juntaId}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #junta.pacienteId)")
    public ResponseEntity<ParticipanteJuntaResponse> addParticipant(@PathVariable Long juntaId,
                                                                     @Valid @RequestBody ParticipanteJuntaCreateRequest request) {
        ParticipanteJuntaResponse response = participanteJuntaService.addParticipant(juntaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/junta/{juntaId}/personal/{personalId}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #junta.pacienteId)")
    public ResponseEntity<Void> removeParticipant(@PathVariable Long juntaId,
                                                   @PathVariable Long personalId) {
        try {
            participanteJuntaService.removeParticipant(juntaId, personalId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}