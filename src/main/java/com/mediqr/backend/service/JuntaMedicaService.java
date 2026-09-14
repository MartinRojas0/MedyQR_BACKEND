package com.mediqr.backend.service;

import com.mediqr.backend.dto.JuntaMedicaCreateRequest;
import com.mediqr.backend.dto.JuntaMedicaResponse;
import com.mediqr.backend.dto.JuntaMedicaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.JuntaMedica;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.JuntaMedicaRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class JuntaMedicaService {

    private final JuntaMedicaRepository juntaMedicaRepository;
    private final PacienteRepository pacienteRepository;
    private final PersonalSaludRepository personalSaludRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    public JuntaMedicaService(JuntaMedicaRepository juntaMedicaRepository,
                              PacienteRepository pacienteRepository,
                              PersonalSaludRepository personalSaludRepository,
                              RegistroAccesoService registroAccesoService,
                              CurrentUserService currentUserService) {
        this.juntaMedicaRepository = juntaMedicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.personalSaludRepository = personalSaludRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<JuntaMedicaResponse> findByPacienteId(Long pacienteId) {
        return juntaMedicaRepository.findByPacienteId(pacienteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JuntaMedicaResponse> findByCreadorId(Long creadorId) {
        return juntaMedicaRepository.findByCreadorId(creadorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JuntaMedicaResponse> findByParticipanteId(Long personalId) {
        // Find juntas where this personal is a participant
        // This requires a join query, but we'll implement it via repository
        // For now, we can use the existing approach
        return List.of();
    }

    public Optional<JuntaMedicaResponse> findById(Long id) {
        return juntaMedicaRepository.findById(id).map(this::toResponse);
    }

    public JuntaMedicaResponse create(JuntaMedicaCreateRequest request) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !"PERSONAL_SALUD".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un profesional de salud puede crear una junta médica");
        }

        // Validate patient exists
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        // Get the creator's PersonalSalud
        PersonalSalud creador = personalSaludRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado para el usuario autenticado"));

        // Validate dates
        if (request.getFechaInicio().isAfter(request.getFechaExpiracion())) {
            throw new BusinessRuleException("La fecha de inicio no puede ser posterior a la fecha de expiración");
        }

        if (request.getFechaExpiracion().isBefore(OffsetDateTime.now())) {
            throw new BusinessRuleException("La fecha de expiración no puede ser en el pasado");
        }

        JuntaMedica junta = new JuntaMedica();
        junta.setPacienteId(request.getPacienteId());
        junta.setCreadorId(creador.getId());
        junta.setMotivo(request.getMotivo());
        junta.setFechaInicio(request.getFechaInicio());
        junta.setFechaExpiracion(request.getFechaExpiracion());
        junta.setEstado("ACTIVA");

        JuntaMedica saved = juntaMedicaRepository.save(junta);

        // Register access
        registrarAcceso(saved.getPacienteId(), "JUNTA_MEDICA", "CREACION_JUNTA");

        return toResponse(saved);
    }

    public JuntaMedicaResponse update(Long id, JuntaMedicaUpdateRequest request) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !"PERSONAL_SALUD".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un profesional de salud puede actualizar una junta médica");
        }

        JuntaMedica junta = juntaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Junta médica no encontrada"));

        // Verify ownership - only creator can update
        PersonalSalud creador = personalSaludRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado"));

        if (!junta.getCreadorId().equals(creador.getId())) {
            throw new IllegalArgumentException("Solo el creador de la junta puede modificarla");
        }

        // Validate dates if provided
        if (request.getFechaInicio() != null && request.getFechaExpiracion() != null) {
            if (request.getFechaInicio().isAfter(request.getFechaExpiracion())) {
                throw new BusinessRuleException("La fecha de inicio no puede ser posterior a la fecha de expiración");
            }
        } else if (request.getFechaInicio() != null) {
            if (request.getFechaInicio().isAfter(junta.getFechaExpiracion())) {
                throw new BusinessRuleException("La fecha de inicio no puede ser posterior a la fecha de expiración");
            }
        } else if (request.getFechaExpiracion() != null) {
            if (request.getFechaExpiracion().isBefore(junta.getFechaInicio())) {
                throw new BusinessRuleException("La fecha de expiración no puede ser anterior a la fecha de inicio");
            }
        }

        if (request.getMotivo() != null) {
            junta.setMotivo(request.getMotivo());
        }
        if (request.getFechaInicio() != null) {
            junta.setFechaInicio(request.getFechaInicio());
        }
        if (request.getFechaExpiracion() != null) {
            if (request.getFechaExpiracion().isBefore(OffsetDateTime.now())) {
                throw new BusinessRuleException("La fecha de expiración no puede ser en el pasado");
            }
            junta.setFechaExpiracion(request.getFechaExpiracion());
        }
        if (request.getEstado() != null) {
            junta.setEstado(request.getEstado());
        }

        JuntaMedica saved = juntaMedicaRepository.save(junta);

        registrarAcceso(saved.getPacienteId(), "JUNTA_MEDICA", "ACTUALIZACION_JUNTA");

        return toResponse(saved);
    }

    public JuntaMedicaResponse closeJunta(Long id) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !"PERSONAL_SALUD".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un profesional de salud puede cerrar una junta médica");
        }

        JuntaMedica junta = juntaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Junta médica no encontrada"));

        PersonalSalud creador = personalSaludRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado"));

        if (!junta.getCreadorId().equals(creador.getId())) {
            throw new IllegalArgumentException("Solo el creador de la junta puede cerrarla");
        }

        if (!"ACTIVA".equals(junta.getEstado())) {
            throw new BusinessRuleException("Solo se pueden cerrar juntas ACTIVAS");
        }

        junta.setEstado("CERRADA");
        JuntaMedica saved = juntaMedicaRepository.save(junta);

        registrarAcceso(saved.getPacienteId(), "JUNTA_MEDICA", "CIERRE_JUNTA");

        return toResponse(saved);
    }

    public void deleteById(Long id) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !"PERSONAL_SALUD".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un profesional de salud puede eliminar una junta médica");
        }

        JuntaMedica junta = juntaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Junta médica no encontrada"));

        PersonalSalud creador = personalSaludRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado"));

        if (!junta.getCreadorId().equals(creador.getId())) {
            throw new IllegalArgumentException("Solo el creador de la junta puede eliminarla");
        }

        Long pacienteId = junta.getPacienteId();
        juntaMedicaRepository.deleteById(id);

        registrarAcceso(pacienteId, "JUNTA_MEDICA", "ELIMINACION_JUNTA");
    }

    public boolean isJuntaActiveAndValid(Long juntaId) {
        return juntaMedicaRepository.findById(juntaId)
                .map(j -> "ACTIVA".equals(j.getEstado())
                        && j.getFechaInicio().isBefore(OffsetDateTime.now())
                        && j.getFechaExpiracion().isAfter(OffsetDateTime.now()))
                .orElse(false);
    }

    public boolean isParticipant(Long juntaId, Long personalId) {
        // This would need a ParticipanteJuntaRepository query
        return false; // Placeholder
    }

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }

    private JuntaMedicaResponse toResponse(JuntaMedica junta) {
        PersonalSalud creador = personalSaludRepository.findById(junta.getCreadorId()).orElse(null);
        return new JuntaMedicaResponse(
                junta.getId(),
                junta.getPacienteId(),
                junta.getCreadorId(),
                junta.getMotivo(),
                junta.getFechaInicio(),
                junta.getFechaExpiracion(),
                junta.getEstado(),
                junta.getCreatedAt(),
                junta.getUpdatedAt()
        );
    }
}