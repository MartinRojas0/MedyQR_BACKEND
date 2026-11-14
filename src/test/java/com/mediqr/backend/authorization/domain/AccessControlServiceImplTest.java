package com.mediqr.backend.authorization.domain;

import com.mediqr.backend.model.AutorizacionPaciente;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.AutorizacionPacienteRepository;
import com.mediqr.backend.repository.JuntaMedicaRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceImplTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PersonalSaludRepository personalSaludRepository;

    @Mock
    private AutorizacionPacienteRepository autorizacionPacienteRepository;

    @Mock
    private JuntaMedicaRepository juntaMedicaRepository;

    private AccessControlServiceImpl accessControlService;

    @BeforeEach
    void setUp() {
        accessControlService = new AccessControlServiceImpl(
                pacienteRepository, personalSaludRepository, autorizacionPacienteRepository, juntaMedicaRepository);
    }

    @Test
    void pacienteCanAccessOwnPatient() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                1L, "paciente@test.com", "PACIENTE", true);

        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setUsuarioId(1L);

        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));

        assertTrue(accessControlService.canAccessPatient(currentUser, 10L));
    }

    @Test
    void pacienteCannotAccessOtherPatient() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                1L, "paciente@test.com", "PACIENTE", true);

        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setUsuarioId(1L);

        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));

        assertFalse(accessControlService.canAccessPatient(currentUser, 20L));
    }

    @Test
    void pacienteWithoutPacienteProfileCannotAccess() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                1L, "paciente@test.com", "PACIENTE", true);

        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        assertFalse(accessControlService.canAccessPatient(currentUser, 10L));
    }

    @Test
    void personalSaludWithActiveAuthorizationCanAccess() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                2L, "doctor@test.com", "PERSONAL_SALUD", true);

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);
        personal.setUsuarioId(2L);

        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(personal));
        when(autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(10L, 5L, "ACTIVA"))
                .thenReturn(true);
        when(juntaMedicaRepository.existsActiveByPacienteIdAndParticipante(10L, 5L)).thenReturn(false);

        assertTrue(accessControlService.canAccessPatient(currentUser, 10L));
    }

    @Test
    void personalSaludWithActiveJuntaMedicaCanAccess() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                2L, "doctor@test.com", "PERSONAL_SALUD", true);

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);
        personal.setUsuarioId(2L);

        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(personal));
        when(autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(10L, 5L, "ACTIVA"))
                .thenReturn(false);
        when(juntaMedicaRepository.existsActiveByPacienteIdAndParticipante(10L, 5L)).thenReturn(true);

        assertTrue(accessControlService.canAccessPatient(currentUser, 10L));
    }

    @Test
    void personalSaludWithoutAuthorizationCannotAccess() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                2L, "doctor@test.com", "PERSONAL_SALUD", true);

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);
        personal.setUsuarioId(2L);

        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(personal));
        when(autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(10L, 5L, "ACTIVA"))
                .thenReturn(false);
        when(juntaMedicaRepository.existsActiveByPacienteIdAndParticipante(10L, 5L)).thenReturn(false);

        assertFalse(accessControlService.canAccessPatient(currentUser, 10L));
    }

    @Test
    void personalSaludWithRevokedAuthorizationCannotAccess() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                2L, "doctor@test.com", "PERSONAL_SALUD", true);

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);
        personal.setUsuarioId(2L);

        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(personal));
        when(autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(10L, 5L, "ACTIVA"))
                .thenReturn(false);
        when(juntaMedicaRepository.existsActiveByPacienteIdAndParticipante(10L, 5L)).thenReturn(false);

        assertFalse(accessControlService.canAccessPatient(currentUser, 10L));
    }

    @Test
    void personalSaludWithoutPersonalSaludProfileCannotAccess() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                2L, "doctor@test.com", "PERSONAL_SALUD", true);

        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.empty());

        assertFalse(accessControlService.canAccessPatient(currentUser, 10L));
    }

    @Test
    void nullCurrentUserDenied() {
        assertFalse(accessControlService.canAccessPatient(null, 10L));
    }

    @Test
    void nullPacienteIdDenied() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                1L, "paciente@test.com", "PACIENTE", true);
        assertFalse(accessControlService.canAccessPatient(currentUser, null));
    }

    @Test
    void unknownRoleDenied() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(
                3L, "unknown@test.com", "UNKNOWN_ROLE", true);

        assertFalse(accessControlService.canAccessPatient(currentUser, 10L));
    }
}