package com.mediqr.backend.service;

import com.mediqr.backend.dto.CitaCreateRequest;
import com.mediqr.backend.dto.CitaEstadoRequest;
import com.mediqr.backend.dto.CitaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Cita;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.repository.CitaRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final PersonalSaludRepository personalSaludRepository;

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA");

    public CitaService(CitaRepository citaRepository,
                       PacienteRepository pacienteRepository,
                       PersonalSaludRepository personalSaludRepository) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.personalSaludRepository = personalSaludRepository;
    }

    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findById(Long id) {
        return citaRepository.findById(id);
    }

    public Cita create(CitaCreateRequest request) {
        validateReferences(request.getPacienteId(), request.getPersonalId());
        validateAvailability(request.getPacienteId(), request.getPersonalId(), request.getFechaHora());

        Cita cita = new Cita();
        cita.setPacienteId(request.getPacienteId());
        cita.setPersonalId(request.getPersonalId());
        cita.setFechaHora(request.getFechaHora());
        cita.setMotivo(request.getMotivo());
        cita.setObservaciones(request.getObservaciones());
        return citaRepository.save(cita);
    }

    public Cita getById(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    public Cita update(Long id, CitaUpdateRequest request) {
        Cita cita = getById(id);
        validateReferences(null, request.getPersonalId());
        validateAvailability(cita.getPacienteId(), request.getPersonalId(), request.getFechaHora(), id);

        cita.setPersonalId(request.getPersonalId());
        cita.setFechaHora(request.getFechaHora());
        cita.setMotivo(request.getMotivo());
        cita.setObservaciones(request.getObservaciones());
        return citaRepository.save(cita);
    }

    public Cita updateEstado(Long id, CitaEstadoRequest request) {
        Cita cita = getById(id);
        if (!ESTADOS_VALIDOS.contains(request.getEstado())) {
            throw new BusinessRuleException("Estado de cita no válido");
        }
        cita.setEstado(request.getEstado());
        return citaRepository.save(cita);
    }

    public boolean isAvailable(Long personalId, OffsetDateTime fechaHora) {
        if (!personalSaludRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal de salud no encontrado");
        }
        return !citaRepository.existsByPersonalIdAndFechaHora(personalId, fechaHora);
    }

    public void deleteById(Long id) {
        getById(id);
        citaRepository.deleteById(id);
    }

    private void validateReferences(Long pacienteId, Long personalId) {
        if (pacienteId != null && !pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
        if (!personalSaludRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal de salud no encontrado");
        }
    }

    private void validateAvailability(Long pacienteId, Long personalId, OffsetDateTime fechaHora) {
        validateAvailability(pacienteId, personalId, fechaHora, null);
    }

    private void validateAvailability(Long pacienteId, Long personalId, OffsetDateTime fechaHora, Long excludedId) {
        boolean personalOcupado = excludedId == null
                ? citaRepository.existsByPersonalIdAndFechaHora(personalId, fechaHora)
                : citaRepository.existsByPersonalIdAndFechaHoraAndIdNot(personalId, fechaHora, excludedId);
        boolean pacienteOcupado = excludedId == null
                ? citaRepository.existsByPacienteIdAndFechaHora(pacienteId, fechaHora)
                : citaRepository.existsByPacienteIdAndFechaHoraAndIdNot(pacienteId, fechaHora, excludedId);
        if (personalOcupado) {
            throw new BusinessRuleException("El personal de salud no está disponible en esa fecha y hora");
        }
        if (pacienteOcupado) {
            throw new BusinessRuleException("El paciente ya tiene una cita en esa fecha y hora");
        }
    }
}
