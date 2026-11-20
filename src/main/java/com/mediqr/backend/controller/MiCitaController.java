package com.mediqr.backend.controller;

import com.mediqr.backend.dto.CitaPacienteCreateRequest;
import com.mediqr.backend.dto.CitaPacienteRescheduleRequest;
import com.mediqr.backend.dto.CitaPacienteResponse;
import com.mediqr.backend.dto.PersonalSaludCitaOptionResponse;
import com.mediqr.backend.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas/me")
@PreAuthorize("hasRole('PACIENTE')")
public class MiCitaController {

    private final CitaService citaService;

    public MiCitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public List<CitaPacienteResponse> listMine() {
        return citaService.listMine();
    }

    @GetMapping("/{id}")
    public CitaPacienteResponse getMine(@PathVariable Long id) {
        return citaService.getMine(id);
    }

    @GetMapping("/opciones-personal")
    public List<PersonalSaludCitaOptionResponse> listProfessionals() {
        return citaService.listActiveProfessionals();
    }

    @GetMapping("/disponibilidad")
    public AvailabilityResponse availability(
            @RequestParam Long personalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHora) {
        return new AvailabilityResponse(citaService.isAvailableForPatient(personalId, fechaHora));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaPacienteResponse createMine(@Valid @RequestBody CitaPacienteCreateRequest request) {
        return citaService.createMine(request);
    }

    @PutMapping("/{id}")
    public CitaPacienteResponse rescheduleMine(
            @PathVariable Long id,
            @Valid @RequestBody CitaPacienteRescheduleRequest request) {
        return citaService.rescheduleMine(id, request);
    }

    @PatchMapping("/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelMine(@PathVariable Long id) {
        citaService.cancelMine(id);
    }

    public record AvailabilityResponse(boolean disponible) {
    }
}
