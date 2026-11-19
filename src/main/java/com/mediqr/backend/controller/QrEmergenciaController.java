package com.mediqr.backend.controller;

import com.mediqr.backend.dto.QrEmergenciaCreateRequest;
import com.mediqr.backend.dto.QrEmergenciaUpdateRequest;
import com.mediqr.backend.model.QrEmergencia;
import com.mediqr.backend.service.QrEmergenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qr-emergencia")
public class QrEmergenciaController {

    private final QrEmergenciaService qrEmergenciaService;

    public QrEmergenciaController(QrEmergenciaService qrEmergenciaService) {
        this.qrEmergenciaService = qrEmergenciaService;
    }

    @GetMapping
    public List<QrEmergencia> getAll() {
        return qrEmergenciaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QrEmergencia> getById(@PathVariable Long id) {
        return ResponseEntity.ok(qrEmergenciaService.getById(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<QrEmergencia> getByPaciente(@PathVariable Long pacienteId) {
        return qrEmergenciaService.findByPacienteId(pacienteId);
    }

    @PostMapping
    public ResponseEntity<QrEmergencia> create(
            @Valid @RequestBody QrEmergenciaCreateRequest request) {
        QrEmergencia savedQrEmergencia = qrEmergenciaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedQrEmergencia);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QrEmergencia> update(
            @PathVariable Long id, @Valid @RequestBody QrEmergenciaUpdateRequest request) {
        return ResponseEntity.ok(qrEmergenciaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        qrEmergenciaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
