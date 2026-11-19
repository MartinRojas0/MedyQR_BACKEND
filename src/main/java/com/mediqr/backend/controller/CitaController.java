package com.mediqr.backend.controller;

import com.mediqr.backend.dto.CitaCreateRequest;
import com.mediqr.backend.dto.CitaEstadoRequest;
import com.mediqr.backend.dto.CitaUpdateRequest;
import com.mediqr.backend.model.Cita;
import com.mediqr.backend.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public List<Cita> getAll() {
        return citaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getById(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Cita> create(@Valid @RequestBody CitaCreateRequest request) {
        Cita savedCita = citaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCita);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> update(@PathVariable Long id, @Valid @RequestBody CitaUpdateRequest request) {
        return ResponseEntity.ok(citaService.update(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Cita> updateEstado(@PathVariable Long id,
                                             @Valid @RequestBody CitaEstadoRequest request) {
        return ResponseEntity.ok(citaService.updateEstado(id, request));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> disponibilidad(
            @RequestParam Long personalId,
            @RequestParam OffsetDateTime fechaHora) {
        return ResponseEntity.ok(new DisponibilidadResponse(
                citaService.isAvailable(personalId, fechaHora)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        citaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record DisponibilidadResponse(boolean disponible) {
    }
}
