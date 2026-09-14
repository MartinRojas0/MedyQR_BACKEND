package com.mediqr.backend.service;

import com.mediqr.backend.dto.AutorizacionPacienteCreateRequest;
import com.mediqr.backend.dto.AutorizacionPacienteResponse;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.AutorizacionPaciente;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.AutorizacionPacienteRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import com.mediqr.backend.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AutorizacionPacienteService {

    private final AutorizacionPacienteRepository autorizacionPacienteRepository;
    private final PacienteRepository pacienteRepository;
    private final PersonalSaludRepository personalSaludRepository;
    private final UsuarioRepository usuarioRepository;

    public AutorizacionPacienteService(AutorizacionPacienteRepository autorizacionPacienteRepository,
                                       PacienteRepository pacienteRepository,
                                       PersonalSaludRepository personalSaludRepository,
                                       UsuarioRepository usuarioRepository) {
        this.autorizacionPacienteRepository = autorizacionPacienteRepository;
        this.pacienteRepository = pacienteRepository;
        this.personalSaludRepository = personalSaludRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<AutorizacionPacienteResponse> findByPacienteId(Long pacienteId) {
        return autorizacionPacienteRepository.findByPacienteId(pacienteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AutorizacionPacienteResponse> findByPersonalId(Long personalId) {
        return autorizacionPacienteRepository.findByPersonalId(personalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<AutorizacionPacienteResponse> findById(Long id) {
        return autorizacionPacienteRepository.findById(id).map(this::toResponse);
    }

    public AutorizacionPacienteResponse createForPatient(CurrentUserService.CurrentUser currentUser,
                                                          Long pacienteId,
                                                          AutorizacionPacienteCreateRequest request) {
        // Solo PACIENTE puede crear autorizaciones
        if (!"PACIENTE".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un paciente puede crear autorizaciones");
        }

        // Verificar que el paciente pertenece al usuario autenticado
        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("El paciente no existe para este usuario"));

        if (!paciente.getId().equals(pacienteId)) {
            throw new IllegalArgumentException("No puedes autorizar acceso a un paciente que no es tuyo");
        }

        // Verificar que el personal existe
        PersonalSalud personal = personalSaludRepository.findById(request.getPersonalId())
                .orElseThrow(() -> new ResourceNotFoundException("El personal de salud no existe"));

        // Verificar si ya existe una autorización ACTIVA
        if (autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(pacienteId, personal.getId(), "ACTIVA")) {
            throw new IllegalArgumentException("Ya existe una autorización ACTIVA para este personal de salud");
        }

        // Verificar si existe una REVOCADA para reactivar
        Optional<AutorizacionPaciente> existente = autorizacionPacienteRepository.findByPacienteIdAndPersonalId(pacienteId, personal.getId());
        if (existente.isPresent() && "REVOCADA".equals(existente.get().getEstado())) {
            // Reactivar la existente
            AutorizacionPaciente a = existente.get();
            a.setEstado("ACTIVA");
            a.setFechaAutorizacion(OffsetDateTime.now());
            a.setFechaRevocacion(null);
            return toResponse(autorizacionPacienteRepository.save(a));
        }

        // Crear nueva autorización
        AutorizacionPaciente autorizacion = new AutorizacionPaciente();
        autorizacion.setPacienteId(pacienteId);
        autorizacion.setPersonalId(personal.getId());
        autorizacion.setEstado("ACTIVA");
        autorizacion.setFechaAutorizacion(OffsetDateTime.now());

        return toResponse(autorizacionPacienteRepository.save(autorizacion));
    }

    public AutorizacionPacienteResponse revokeForPatient(CurrentUserService.CurrentUser currentUser, Long autorizacionId) {
        // Solo PACIENTE puede revocar
        if (!"PACIENTE".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un paciente puede revocar autorizaciones");
        }

        AutorizacionPaciente autorizacion = autorizacionPacienteRepository.findById(autorizacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada"));

        // Verificar que el paciente pertenece al usuario autenticado
        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("El paciente no existe para este usuario"));

        if (!autorizacion.getPacienteId().equals(paciente.getId())) {
            throw new IllegalArgumentException("No puedes revocar una autorización de un paciente que no es tuyo");
        }

        if (!"ACTIVA".equals(autorizacion.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden revocar autorizaciones ACTIVAS");
        }

        autorizacion.setEstado("REVOCADA");
        autorizacion.setFechaRevocacion(OffsetDateTime.now());

        return toResponse(autorizacionPacienteRepository.save(autorizacion));
    }

    private AutorizacionPacienteResponse toResponse(AutorizacionPaciente a) {
        PersonalSalud personal = personalSaludRepository.findById(a.getPersonalId()).orElse(null);
        return new AutorizacionPacienteResponse(
                a.getId(),
                a.getPacienteId(),
                a.getPersonalId(),
                personal != null ? personal.getNombres() : null,
                personal != null ? personal.getApellidos() : null,
                a.getEstado(),
                a.getFechaAutorizacion(),
                a.getFechaRevocacion(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
