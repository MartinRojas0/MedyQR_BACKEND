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

@Service
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
        if (currentUser == null || pacienteId == null) {
            return false;
        }

        // PACIENTE: solo puede acceder a su propio paciente
        if ("PACIENTE".equals(currentUser.rol())) {
            return pacienteRepository.findByUsuarioId(currentUser.userId())
                    .map(p -> p.getId().equals(pacienteId))
                    .orElse(false);
        }

        // PERSONAL_SALUD: requiere autorización ACTIVA O participación en Junta Médica ACTIVA y vigente
        if ("PERSONAL_SALUD".equals(currentUser.rol())) {
            return personalSaludRepository.findByUsuarioId(currentUser.userId())
                    .map(personal -> {
                        // Opción 1: AutorizaciónPaciente ACTIVA
                        boolean tieneAutorizacion = autorizacionPacienteRepository
                                .existsByPacienteIdAndPersonalIdAndEstado(pacienteId, personal.getId(), "ACTIVA");

                        // Opción 2: Junta Médica ACTIVA y vigente donde es participante
                        boolean tieneJuntaMedica = juntaMedicaRepository
                                .existsActiveByPacienteIdAndParticipante(pacienteId, personal.getId());

                        return tieneAutorizacion || tieneJuntaMedica;
                    })
                    .orElse(false);
        }

        // Cualquier otro rol: denegar
        return false;
    }
}