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
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
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
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA");

    public CitaService(CitaRepository citaRepository,
                       PacienteRepository pacienteRepository,
                       PersonalSaludRepository personalSaludRepository,
                       RegistroAccesoService registroAccesoService,
                       CurrentUserService currentUserService) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.personalSaludRepository = personalSaludRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
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
        
        Cita saved = citaRepository.save(cita);
        
        registrarAcceso(saved.getPacienteId(), "CITA", "CREACION_CITA");

        return saved;
    }

    public Cita getById(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        registrarAcceso(cita.getPacienteId(), "CITA", "LECTURA_CITA");

        return cita;
    }

    public Cita update(Long id, CitaUpdateRequest request) {
        Cita cita = getById(id);
        validateReferences(null, request.getPersonalId());
        validateAvailability(cita.getPacienteId(), request.getPersonalId(), request.getFechaHora(), id);

        cita.setPersonalId(request.getPersonalId());
        cita.setFechaHora(request.getFechaHora());
        cita.setMotivo(request.getMotivo());
        cita.setObservaciones(request.getObservaciones());
        
        Cita saved = citaRepository.save(cita);
        
        registrarAcceso(saved.getPacienteId(), "CITA", "ACTUALIZACION_CITA");

        return saved;
    }

    public Cita updateEstado(Long id, CitaEstadoRequest request) {
        Cita cita = getById(id);
        if (!ESTADOS_VALIDOS.contains(request.getEstado())) {
            throw new BusinessRuleException("Estado de cita no válido");
        }
        cita.setEstado(request.getEstado());
        
        Cita saved = citaRepository.save(cita);
        
        registrarAcceso(saved.getPacienteId(), "CITA", "CAMBIO_ESTADO_CITA");

        return saved;
    }

    public boolean isAvailable(Long personalId, OffsetDateTime fechaHora) {
        if (!personalSaludRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal de salud no encontrado");
        }
        return !citaRepository.existsByPersonalIdAndFechaHora(personalId, fechaHora);
    }

    public void deleteById(Long id) {
        Cita cita = getById(id);
        Long pacienteId = cita.getPacienteId();
        citaRepository.deleteById(id);
        
        registrarAcceso(pacienteId, "CITA", "ELIMINACION_CITA");
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

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }
}