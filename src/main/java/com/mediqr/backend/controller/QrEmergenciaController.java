package com.mediqr.backend.controller;

import com.mediqr.backend.model.QrEmergencia;
import com.mediqr.backend.service.QrEmergenciaService;
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
        return qrEmergenciaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<QrEmergencia> create(@RequestBody QrEmergencia qrEmergencia) {
        QrEmergencia savedQrEmergencia = qrEmergenciaService.save(qrEmergencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedQrEmergencia);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QrEmergencia> update(@PathVariable Long id, @RequestBody QrEmergencia qrEmergencia) {
        return qrEmergenciaService.update(id, qrEmergencia)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (qrEmergenciaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        qrEmergenciaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
