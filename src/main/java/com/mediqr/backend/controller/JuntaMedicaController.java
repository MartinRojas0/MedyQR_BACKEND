package com.mediqr.backend.controller;

import com.mediqr.backend.dto.JuntaMedicaCreateRequest;
import com.mediqr.backend.dto.JuntaMedicaResponse;
import com.mediqr.backend.dto.JuntaMedicaUpdateRequest;
import com.mediqr.backend.service.JuntaMedicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/juntas-medicas")
public class JuntaMedicaController {

    private final JuntaMedicaService juntaMedicaService;

    public JuntaMedicaController(JuntaMedicaService juntaMedicaService) {
        this.juntaMedicaService = juntaMedicaService;
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #pacienteId)")
    public List<JuntaMedicaResponse> getByPacienteId(@PathVariable Long pacienteId) {
        return juntaMedicaService.findByPacienteId(pacienteId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #junta.pacienteId)")
    public ResponseEntity<JuntaMedicaResponse> getById(@PathVariable Long id) {
        return juntaMedicaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public ResponseEntity<JuntaMedicaResponse> create(@Valid @RequestBody JuntaMedicaCreateRequest request) {
        JuntaMedicaResponse saved = juntaMedicaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessControl.canAccessPatient(authentication, #junta.pacienteId)")
    public ResponseEntity<JuntaMedicaResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody JuntaMedicaUpdateRequest request) {
        JuntaMedicaResponse updated = juntaMedicaService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public ResponseEntity<JuntaMedicaResponse> closeJunta(@PathVariable Long id) {
        JuntaMedicaResponse closed = juntaMedicaService.closeJunta(id);
        return ResponseEntity.ok(closed);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (juntaMedicaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        juntaMedicaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}