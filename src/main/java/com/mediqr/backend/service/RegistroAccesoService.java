package com.mediqr.backend.service;

import com.mediqr.backend.model.RegistroAcceso;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.RegistroAccesoRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RegistroAccesoService {

    private final RegistroAccesoRepository registroAccesoRepository;
    private final PersonalSaludRepository personalSaludRepository;

    public RegistroAccesoService(RegistroAccesoRepository registroAccesoRepository,
                                 PersonalSaludRepository personalSaludRepository) {
        this.registroAccesoRepository = registroAccesoRepository;
        this.personalSaludRepository = personalSaludRepository;
    }

    public List<RegistroAcceso> findByPacienteId(Long pacienteId) {
        return registroAccesoRepository.findByPacienteId(pacienteId);
    }

    public List<RegistroAcceso> findByPersonalId(Long personalId) {
        return registroAccesoRepository.findByPersonalId(personalId);
    }

    public Optional<RegistroAcceso> findById(Long id) {
        return registroAccesoRepository.findById(id);
    }

    public RegistroAcceso registrarAcceso(CurrentUserService.CurrentUser currentUser,
                                          Long pacienteId,
                                          String tipoAcceso,
                                          String accion,
                                          UUID identificadorQr) {
        Long personalId = null;
        if ("PERSONAL_SALUD".equals(currentUser.rol())) {
            personalId = personalSaludRepository.findByUsuarioId(currentUser.userId())
                    .map(PersonalSalud::getId)
                    .orElse(null);
        }

        RegistroAcceso registro = new RegistroAcceso();
        registro.setPacienteId(pacienteId);
        registro.setPersonalId(personalId);
        registro.setTipoAcceso(tipoAcceso);
        registro.setAccion(accion);
        registro.setFechaAcceso(OffsetDateTime.now());
        registro.setIdentificadorQr(identificadorQr);

        return registroAccesoRepository.save(registro);
    }

    public void deleteById(Long id) {
        registroAccesoRepository.deleteById(id);
    }
}
