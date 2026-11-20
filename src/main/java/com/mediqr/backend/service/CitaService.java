package com.mediqr.backend.service;

import com.mediqr.backend.dto.CitaCreateRequest;
import com.mediqr.backend.dto.CitaEstadoRequest;
import com.mediqr.backend.dto.CitaUpdateRequest;
import com.mediqr.backend.dto.CitaPacienteCreateRequest;
import com.mediqr.backend.dto.CitaPacienteRescheduleRequest;
import com.mediqr.backend.dto.CitaPacienteResponse;
import com.mediqr.backend.dto.PersonalSaludCitaOptionResponse;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Cita;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.repository.CitaRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import com.mediqr.backend.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mediqr.backend.service.RegistroAccesoService;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final PersonalSaludRepository personalSaludRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA");
    private static final Set<String> ESTADOS_QUE_RESERVAN_HORARIO = Set.of("PENDIENTE", "CONFIRMADA");

    public CitaService(CitaRepository citaRepository,
                       PacienteRepository pacienteRepository,
                       PersonalSaludRepository personalSaludRepository,
                       UsuarioRepository usuarioRepository,
                       RegistroAccesoService registroAccesoService,
                       CurrentUserService currentUserService) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.personalSaludRepository = personalSaludRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findById(Long id) {
        return citaRepository.findById(id);
    }

    public Long findPacienteIdById(Long id) {
        return citaRepository.findById(id)
                .map(Cita::getPacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    public Cita create(CitaCreateRequest request) {
        validateFutureDate(request.getFechaHora());
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
        validateFutureDate(request.getFechaHora());
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
        validateFutureDate(fechaHora);
        requireActiveProfessional(personalId);
        return !citaRepository.existsByPersonalIdAndFechaHoraAndEstadoIn(
                personalId, fechaHora, ESTADOS_QUE_RESERVAN_HORARIO);
    }

    @Transactional(readOnly = true)
    public List<CitaPacienteResponse> listMine() {
        Long pacienteId = requireCurrentPatientId();
        return citaRepository.findAllByPacienteIdOrderByFechaHoraAsc(pacienteId)
                .stream().map(this::toPatientResponse).toList();
    }

    @Transactional(readOnly = true)
    public CitaPacienteResponse getMine(Long id) {
        Long pacienteId = requireCurrentPatientId();
        Cita cita = findMine(id, pacienteId);
        registrarAcceso(pacienteId, "CITA", "LECTURA_CITA");
        return toPatientResponse(cita);
    }

    @Transactional(readOnly = true)
    public List<PersonalSaludCitaOptionResponse> listActiveProfessionals() {
        requireCurrentPatientId();
        return personalSaludRepository.findAllByActivoTrueOrderByApellidosAscNombresAsc()
                .stream()
                .filter(this::isActiveProfessional)
                .map(personal -> new PersonalSaludCitaOptionResponse(
                        personal.getId(),
                        displayName(personal),
                        personal.getEspecialidad()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isAvailableForPatient(Long personalId, OffsetDateTime fechaHora) {
        Long pacienteId = requireCurrentPatientId();
        validateFutureDate(fechaHora);
        requireActiveProfessional(personalId);
        return !citaRepository.existsByPersonalIdAndFechaHoraAndEstadoIn(
                        personalId, fechaHora, ESTADOS_QUE_RESERVAN_HORARIO)
                && !citaRepository.existsByPacienteIdAndFechaHoraAndEstadoIn(
                        pacienteId, fechaHora, ESTADOS_QUE_RESERVAN_HORARIO);
    }

    @Transactional
    public CitaPacienteResponse createMine(CitaPacienteCreateRequest request) {
        Long pacienteId = requireCurrentPatientId();
        pacienteRepository.findByIdForUpdate(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        PersonalSalud profesional = requireActiveProfessionalForUpdate(request.getPersonalId());
        validateFutureDate(request.getFechaHora());
        validateAvailability(pacienteId, profesional.getId(), request.getFechaHora());

        Cita cita = new Cita();
        cita.setPacienteId(pacienteId);
        cita.setPersonalId(profesional.getId());
        cita.setFechaHora(request.getFechaHora());
        cita.setMotivo(normalize(request.getMotivo()));
        Cita saved = citaRepository.save(cita);
        registrarAcceso(pacienteId, "CITA", "CREACION_CITA");
        return toPatientResponse(saved);
    }

    @Transactional
    public CitaPacienteResponse rescheduleMine(Long id, CitaPacienteRescheduleRequest request) {
        Long pacienteId = requireCurrentPatientId();
        pacienteRepository.findByIdForUpdate(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        Cita cita = citaRepository.findByIdAndPacienteIdForUpdate(id, pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        requireReschedulable(cita);
        personalSaludRepository.findByIdForUpdate(cita.getPersonalId())
                .filter(profesional -> Boolean.TRUE.equals(profesional.getActivo()))
                .orElseThrow(() -> new BusinessRuleException(
                        "El profesional ya no está disponible para reprogramar esta cita"));
        validateFutureDate(request.getFechaHora());
        validateAvailability(pacienteId, cita.getPersonalId(), request.getFechaHora(), id);
        cita.setFechaHora(request.getFechaHora());
        Cita saved = citaRepository.save(cita);
        registrarAcceso(pacienteId, "CITA", "REPROGRAMACION_CITA");
        return toPatientResponse(saved);
    }

    @Transactional
    public void cancelMine(Long id) {
        Long pacienteId = requireCurrentPatientId();
        pacienteRepository.findByIdForUpdate(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        Cita cita = citaRepository.findByIdAndPacienteIdForUpdate(id, pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        if (!ESTADOS_QUE_RESERVAN_HORARIO.contains(cita.getEstado())
                || !cita.getFechaHora().isAfter(OffsetDateTime.now())) {
            throw new BusinessRuleException("Solo puedes cancelar una cita futura pendiente o confirmada");
        }
        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
        registrarAcceso(pacienteId, "CITA", "CANCELACION_CITA");
    }

    public void deleteById(Long id) {
        Cita cita = getById(id);
        Long pacienteId = cita.getPacienteId();
        citaRepository.deleteById(id);
        
        registrarAcceso(pacienteId, "CITA", "ELIMINACION_CITA");
    }

    private void validateReferences(Long pacienteId, Long personalId) {
        if (pacienteId != null && pacienteRepository.findById(pacienteId)
                .filter(paciente -> Boolean.TRUE.equals(paciente.getActivo())).isEmpty()) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
        requireActiveProfessional(personalId);
    }

    private void validateAvailability(Long pacienteId, Long personalId, OffsetDateTime fechaHora) {
        validateAvailability(pacienteId, personalId, fechaHora, null);
    }

    private void validateAvailability(Long pacienteId, Long personalId, OffsetDateTime fechaHora, Long excludedId) {
        boolean personalOcupado = excludedId == null
                ? citaRepository.existsByPersonalIdAndFechaHoraAndEstadoIn(
                        personalId, fechaHora, ESTADOS_QUE_RESERVAN_HORARIO)
                : citaRepository.existsByPersonalIdAndFechaHoraAndEstadoInAndIdNot(
                        personalId, fechaHora, ESTADOS_QUE_RESERVAN_HORARIO, excludedId);
        boolean pacienteOcupado = excludedId == null
                ? citaRepository.existsByPacienteIdAndFechaHoraAndEstadoIn(
                        pacienteId, fechaHora, ESTADOS_QUE_RESERVAN_HORARIO)
                : citaRepository.existsByPacienteIdAndFechaHoraAndEstadoInAndIdNot(
                        pacienteId, fechaHora, ESTADOS_QUE_RESERVAN_HORARIO, excludedId);
        if (personalOcupado) {
            throw new BusinessRuleException("El personal de salud no está disponible en esa fecha y hora");
        }
        if (pacienteOcupado) {
            throw new BusinessRuleException("El paciente ya tiene una cita en esa fecha y hora");
        }
    }

    private Long requireCurrentPatientId() {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !"PACIENTE".equals(currentUser.rol())
                || !Boolean.TRUE.equals(currentUser.activo())) {
            throw new AccessDeniedException("Solo un paciente activo puede gestionar sus citas");
        }
        return pacienteRepository.findByUsuarioId(currentUser.userId())
                .filter(paciente -> Boolean.TRUE.equals(paciente.getActivo()))
                .map(paciente -> paciente.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado"));
    }

    private PersonalSalud requireActiveProfessional(Long personalId) {
        return personalSaludRepository.findById(personalId)
                .filter(profesional -> Boolean.TRUE.equals(profesional.getActivo()))
                .filter(this::isActiveProfessional)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado"));
    }

    private PersonalSalud requireActiveProfessionalForUpdate(Long personalId) {
        return personalSaludRepository.findByIdForUpdate(personalId)
                .filter(profesional -> Boolean.TRUE.equals(profesional.getActivo()))
                .filter(this::isActiveProfessional)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado"));
    }

    private boolean isActiveProfessional(PersonalSalud profesional) {
        return profesional.getUsuarioId() != null && usuarioRepository.findById(profesional.getUsuarioId())
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo())
                        && "PERSONAL_SALUD".equals(usuario.getRol()))
                .isPresent();
    }

    private Cita findMine(Long id, Long pacienteId) {
        return citaRepository.findByIdAndPacienteId(id, pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    private void requireReschedulable(Cita cita) {
        if (!"PENDIENTE".equals(cita.getEstado())
                || !cita.getFechaHora().isAfter(OffsetDateTime.now())) {
            throw new BusinessRuleException("Solo puedes reprogramar una cita futura pendiente");
        }
    }

    private void validateFutureDate(OffsetDateTime fechaHora) {
        if (fechaHora == null || !fechaHora.isAfter(OffsetDateTime.now())) {
            throw new BusinessRuleException("La fecha y hora de la cita deben ser futuras");
        }
    }

    private CitaPacienteResponse toPatientResponse(Cita cita) {
        PersonalSalud profesional = personalSaludRepository.findById(cita.getPersonalId()).orElse(null);
        return new CitaPacienteResponse(
                cita.getId(),
                cita.getPersonalId(),
                profesional == null ? "Profesional no disponible" : displayName(profesional),
                profesional == null ? null : profesional.getEspecialidad(),
                cita.getFechaHora(),
                cita.getMotivo(),
                cita.getEstado(),
                cita.getCreatedAt(),
                cita.getUpdatedAt());
    }

    private String displayName(PersonalSalud profesional) {
        return java.util.stream.Stream.of(profesional.getNombres(), profesional.getApellidos())
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }
}