package com.mediqr.backend.authorization.domain;

import com.mediqr.backend.model.AutorizacionPaciente;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.AutorizacionPacienteRepository;
import com.mediqr.backend.repository.JuntaMedicaRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service("accessControl")
public class AccessControlServiceImpl implements AccessControlService {

    private final PacienteRepository pacienteRepository;
    private final PersonalSaludRepository personalSaludRepository;
    private final AutorizacionPacienteRepository autorizacionPacienteRepository;
    private final JuntaMedicaRepository juntaMedicaRepository;

    public AccessControlServiceImpl(PacienteRepository pacienteRepository,
                                    PersonalSaludRepository personalSaludRepository,
                                    AutorizacionPacienteRepository autorizacionPacienteRepository,
                                    JuntaMedicaRepository juntaMedicaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.personalSaludRepository = personalSaludRepository;
        this.autorizacionPacienteRepository = autorizacionPacienteRepository;
        this.juntaMedicaRepository = juntaMedicaRepository;
    }

    @Override
    public boolean canAccessPatient(CurrentUserService.CurrentUser currentUser, Long pacienteId) {
        if (currentUser == null || pacienteId == null || !Boolean.TRUE.equals(currentUser.activo())) {
            return false;
        }

        // PACIENTE: solo puede acceder a su propio paciente
        if ("PACIENTE".equals(currentUser.rol())) {
            return pacienteRepository.findByUsuarioId(currentUser.userId())
                    .map(p -> Boolean.TRUE.equals(p.getActivo()) && p.getId().equals(pacienteId))
                    .orElse(false);
        }

        // A junta membership alone is not patient consent; every professional
        // must have an active patient-granted authorization.
        if ("PERSONAL_SALUD".equals(currentUser.rol())) {
            return personalSaludRepository.findByUsuarioId(currentUser.userId())
                    .map(personal -> Boolean.TRUE.equals(personal.getActivo())
                            && autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(
                                    pacienteId, personal.getId(), "ACTIVA"))
                    .orElse(false);
        }

        // Cualquier otro rol: denegar
        return false;
    }

    @Override
    public boolean canAccessJunta(CurrentUserService.CurrentUser currentUser, Long juntaId) {
        if (juntaId == null) {
            return false;
        }
        return juntaMedicaRepository.findById(juntaId)
                .map(junta -> canAccessPatient(currentUser, junta.getPacienteId()))
                .orElse(false);
    }

    @Override
    public boolean canAccessUser(CurrentUserService.CurrentUser currentUser, Long usuarioId) {
        if (currentUser == null || usuarioId == null) {
            return false;
        }

        // PACIENTE: solo puede acceder a su propio usuario
        if ("PACIENTE".equals(currentUser.rol())) {
            return currentUser.userId().equals(usuarioId);
        }

        // PERSONAL_SALUD: no puede acceder a usuarios (solo a pacientes)
        // En el futuro se podría permitir si tiene autorización, por ahora denegamos
        return false;
    }
}