package com.mediqr.backend.controller;

import com.mediqr.backend.dto.QrEmergenciaCreateRequest;
import com.mediqr.backend.dto.QrEmergenciaEmergencyResponse;
import com.mediqr.backend.dto.QrEmergenciaResponse;
import com.mediqr.backend.dto.QrEmergenciaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.service.QrEmergenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/qr-emergencia")
public class QrEmergenciaController {

    private final QrEmergenciaService qrEmergenciaService;

    public QrEmergenciaController(QrEmergenciaService qrEmergenciaService) {
        this.qrEmergenciaService = qrEmergenciaService;
    }

    @GetMapping("/emergency/{token}")
    public ResponseEntity<QrEmergenciaEmergencyResponse> accessByToken(@PathVariable UUID token) {
        try {
            QrEmergenciaEmergencyResponse response = qrEmergenciaService.accessByToken(token);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessRuleException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public List<QrEmergenciaResponse> getAll() {
        return qrEmergenciaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #qr.pacienteId)")
    public ResponseEntity<QrEmergenciaResponse> getById(@PathVariable Long id) {
        return qrEmergenciaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #pacienteId)")
    public List<QrEmergenciaResponse> getByPaciente(@PathVariable Long pacienteId) {
        return qrEmergenciaService.findByPacienteId(pacienteId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<QrEmergenciaResponse> create(@Valid @RequestBody QrEmergenciaCreateRequest request) {
        QrEmergenciaResponse saved = qrEmergenciaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #qr.pacienteId)")
    public ResponseEntity<QrEmergenciaResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody QrEmergenciaUpdateRequest request) {
        return ResponseEntity.ok(qrEmergenciaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #qr.pacienteId)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (qrEmergenciaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        qrEmergenciaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}