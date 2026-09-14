package com.mediqr.backend.service;

import com.mediqr.backend.dto.ParticipanteJuntaCreateRequest;
import com.mediqr.backend.dto.ParticipanteJuntaResponse;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.JuntaMedica;
import com.mediqr.backend.model.ParticipanteJunta;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.JuntaMedicaRepository;
import com.mediqr.backend.repository.ParticipanteJuntaRepository;
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
public class ParticipanteJuntaService {

    private final ParticipanteJuntaRepository participanteJuntaRepository;
    private final JuntaMedicaRepository juntaMedicaRepository;
    private final PersonalSaludRepository personalSaludRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    public ParticipanteJuntaService(ParticipanteJuntaRepository participanteJuntaRepository,
                                    JuntaMedicaRepository juntaMedicaRepository,
                                    PersonalSaludRepository personalSaludRepository,
                                    RegistroAccesoService registroAccesoService,
                                    CurrentUserService currentUserService) {
        this.participanteJuntaRepository = participanteJuntaRepository;
        this.juntaMedicaRepository = juntaMedicaRepository;
        this.personalSaludRepository = personalSaludRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<ParticipanteJuntaResponse> findByJuntaId(Long juntaId) {
        return participanteJuntaRepository.findByJuntaId(juntaId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ParticipanteJuntaResponse> findByPersonalId(Long personalId) {
        return participanteJuntaRepository.findByPersonalId(personalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<ParticipanteJuntaResponse> findById(Long id) {
        return participanteJuntaRepository.findById(id).map(this::toResponse);
    }

    public ParticipanteJuntaResponse addParticipant(Long juntaId, ParticipanteJuntaCreateRequest request) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !"PERSONAL_SALUD".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un profesional de salud puede agregar participantes");
        }

        JuntaMedica junta = juntaMedicaRepository.findById(juntaId)
                .orElseThrow(() -> new ResourceNotFoundException("Junta médica no encontrada"));

        // Verify the current user is the creator of the junta
        PersonalSalud currentPersonal = personalSaludRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado"));

        if (!junta.getCreadorId().equals(currentPersonal.getId())) {
            throw new IllegalArgumentException("Solo el creador de la junta puede agregar participantes");
        }

        // Validate junta is active and valid
        if (!"ACTIVA".equals(junta.getEstado())) {
            throw new BusinessRuleException("Solo se pueden agregar participantes a juntas ACTIVAS");
        }
        if (junta.getFechaExpiracion().isBefore(OffsetDateTime.now())) {
            throw new BusinessRuleException("No se pueden agregar participantes a una junta expirada");
        }

        // Validate participant exists
        PersonalSalud participante = personalSaludRepository.findById(request.getPersonalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado"));

        // Check if already a participant
        if (participanteJuntaRepository.existsByJuntaIdAndPersonalId(juntaId, request.getPersonalId())) {
            throw new BusinessRuleException("Este profesional ya es participante de la junta");
        }

        // Cannot add the creator as a participant (they're already the creator)
        if (junta.getCreadorId().equals(request.getPersonalId())) {
            throw new BusinessRuleException("El creador de la junta ya está implícitamente incluido");
        }

        ParticipanteJunta participanteJunta = new ParticipanteJunta();
        participanteJunta.setJuntaId(juntaId);
        participanteJunta.setPersonalId(request.getPersonalId());
        participanteJunta.setFechaIngreso(OffsetDateTime.now());

        ParticipanteJunta saved = participanteJuntaRepository.save(participanteJunta);

        // Register access
        registrarAcceso(junta.getPacienteId(), "JUNTA_MEDICA", "AGREGAR_PARTICIPANTE");

        return toResponse(saved);
    }

    public void removeParticipant(Long juntaId, Long personalId) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !"PERSONAL_SALUD".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un profesional de salud puede eliminar participantes");
        }

        JuntaMedica junta = juntaMedicaRepository.findById(juntaId)
                .orElseThrow(() -> new ResourceNotFoundException("Junta médica no encontrada"));

        PersonalSalud currentPersonal = personalSaludRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional de salud no encontrado"));

        if (!junta.getCreadorId().equals(currentPersonal.getId())) {
            throw new IllegalArgumentException("Solo el creador de la junta puede eliminar participantes");
        }

        // Cannot remove the creator
        if (junta.getCreadorId().equals(personalId)) {
            throw new BusinessRuleException("No se puede eliminar al creador de la junta");
        }

        ParticipanteJunta participante = participanteJuntaRepository.findByJuntaIdAndPersonalId(juntaId, personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Participante no encontrado en esta junta"));

        Long pacienteId = junta.getPacienteId();
        participanteJuntaRepository.delete(participante);

        registrarAcceso(pacienteId, "JUNTA_MEDICA", "ELIMINAR_PARTICIPANTE");
    }

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }

    private ParticipanteJuntaResponse toResponse(ParticipanteJunta p) {
        PersonalSalud personal = personalSaludRepository.findById(p.getPersonalId()).orElse(null);
        return new ParticipanteJuntaResponse(
                p.getId(),
                p.getJuntaId(),
                p.getPersonalId(),
                personal != null ? personal.getNombres() : null,
                personal != null ? personal.getApellidos() : null,
                p.getFechaIngreso()
        );
    }
}