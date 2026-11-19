package com.mediqr.backend.service;

import com.mediqr.backend.dto.AutorizacionPacienteCreateRequest;
import com.mediqr.backend.dto.AutorizacionPacienteResponse;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.AutorizacionPaciente;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.AutorizacionPacienteRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.mediqr.backend.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutorizacionPacienteServiceTest {

    @Mock
    private AutorizacionPacienteRepository autorizacionPacienteRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PersonalSaludRepository personalSaludRepository;

    private AutorizacionPacienteService autorizacionPacienteService;

    @BeforeEach
    void setUp() {
        autorizacionPacienteService = new AutorizacionPacienteService(
                autorizacionPacienteRepository, pacienteRepository, personalSaludRepository, null);
    }

    @Test
    void createForPatient_succeedsWhenPatientOwnsPatientAndPersonalExists() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long pacienteId = 10L;

        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setUsuarioId(1L);

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);
        personal.setUsuarioId(2L);
        personal.setNombres("Dr. Juan");
        personal.setApellidos("Perez");

        AutorizacionPacienteCreateRequest request = new AutorizacionPacienteCreateRequest();
        request.setPersonalId(5L);

        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));
        when(personalSaludRepository.findById(5L)).thenReturn(Optional.of(personal));
        when(autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(pacienteId, 5L, "ACTIVA")).thenReturn(false);
        when(autorizacionPacienteRepository.findByPacienteIdAndPersonalId(pacienteId, 5L)).thenReturn(Optional.empty());
        when(autorizacionPacienteRepository.save(any(AutorizacionPaciente.class))).thenAnswer(invocation -> {
            AutorizacionPaciente a = invocation.getArgument(0);
            a.setId(1L);
            a.setFechaAutorizacion(OffsetDateTime.now());
            return a;
        });

        AutorizacionPacienteResponse response = autorizacionPacienteService.createForPatient(currentUser, pacienteId, request);

        assertEquals("ACTIVA", response.getEstado());
        assertEquals(pacienteId, response.getPacienteId());
        assertEquals(5L, response.getPersonalId());
        verify(autorizacionPacienteRepository).save(any(AutorizacionPaciente.class));
    }

    @Test
    void createForPatient_rejectsWhenPersonalSaludTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long pacienteId = 10L;

        AutorizacionPacienteCreateRequest request = new AutorizacionPacienteCreateRequest();
        request.setPersonalId(5L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> autorizacionPacienteService.createForPatient(currentUser, pacienteId, request));

        assertEquals("Solo un paciente puede crear autorizaciones", ex.getMessage());
    }

    @Test
    void createForPatient_rejectsWhenPatientTriesToAuthorizeOtherPatient() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long pacienteId = 999L; // Different from paciente's actual ID

        Paciente paciente = new Paciente();
        paciente.setId(10L); // Patient's actual ID
        paciente.setUsuarioId(1L);

        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));

        AutorizacionPacienteCreateRequest request = new AutorizacionPacienteCreateRequest();
        request.setPersonalId(5L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> autorizacionPacienteService.createForPatient(currentUser, pacienteId, request));

        assertEquals("No puedes autorizar acceso a un paciente que no es tuyo", ex.getMessage());
    }

    @Test
    void createForPatient_rejectsWhenActiveAuthorizationAlreadyExists() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long pacienteId = 10L;

        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setUsuarioId(1L);

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);

        AutorizacionPacienteCreateRequest request = new AutorizacionPacienteCreateRequest();
        request.setPersonalId(5L);

        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));
        when(personalSaludRepository.findById(5L)).thenReturn(Optional.of(personal));
        when(autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(pacienteId, 5L, "ACTIVA")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> autorizacionPacienteService.createForPatient(currentUser, pacienteId, request));

        assertEquals("Ya existe una autorización ACTIVA para este personal de salud", ex.getMessage());
    }

    @Test
    void createForPatient_reactivatesRevokedAuthorization() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long pacienteId = 10L;

        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setUsuarioId(1L);

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);
        personal.setNombres("Dr. Juan");
        personal.setApellidos("Perez");

        AutorizacionPaciente existing = new AutorizacionPaciente();
        existing.setId(1L);
        existing.setPacienteId(pacienteId);
        existing.setPersonalId(5L);
        existing.setEstado("REVOCADA");
        existing.setFechaAutorizacion(OffsetDateTime.now().minusDays(1));
        existing.setFechaRevocacion(OffsetDateTime.now().minusHours(1));

        AutorizacionPacienteCreateRequest request = new AutorizacionPacienteCreateRequest();
        request.setPersonalId(5L);

        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));
        when(personalSaludRepository.findById(5L)).thenReturn(Optional.of(personal));
        when(autorizacionPacienteRepository.existsByPacienteIdAndPersonalIdAndEstado(pacienteId, 5L, "ACTIVA")).thenReturn(false);
        when(autorizacionPacienteRepository.findByPacienteIdAndPersonalId(pacienteId, 5L)).thenReturn(Optional.of(existing));
        when(autorizacionPacienteRepository.save(any(AutorizacionPaciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AutorizacionPacienteResponse response = autorizacionPacienteService.createForPatient(currentUser, pacienteId, request);

        assertEquals("ACTIVA", response.getEstado());
        assertEquals(pacienteId, response.getPacienteId());
        assertEquals(5L, response.getPersonalId());
        verify(autorizacionPacienteRepository).save(any(AutorizacionPaciente.class));
    }

    @Test
    void revokeForPatient_succeedsWhenPatientOwnsAuthorization() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long autorizacionId = 1L;

        AutorizacionPaciente autorizacion = new AutorizacionPaciente();
        autorizacion.setId(autorizacionId);
        autorizacion.setPacienteId(10L);
        autorizacion.setPersonalId(5L);
        autorizacion.setEstado("ACTIVA");
        autorizacion.setFechaAutorizacion(OffsetDateTime.now().minusDays(1));

        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setUsuarioId(1L);

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);
        personal.setNombres("Dr. Juan");
        personal.setApellidos("Perez");

        when(autorizacionPacienteRepository.findById(autorizacionId)).thenReturn(Optional.of(autorizacion));
        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));
        when(personalSaludRepository.findById(5L)).thenReturn(Optional.of(personal));
        when(autorizacionPacienteRepository.save(any(AutorizacionPaciente.class))).thenAnswer(invocation -> {
            AutorizacionPaciente a = invocation.getArgument(0);
            a.setFechaRevocacion(OffsetDateTime.now());
            return a;
        });

        AutorizacionPacienteResponse response = autorizacionPacienteService.revokeForPatient(currentUser, autorizacionId);

        assertEquals("REVOCADA", response.getEstado());
        assertEquals(10L, response.getPacienteId());
        verify(autorizacionPacienteRepository).save(any(AutorizacionPaciente.class));
    }

    @Test
    void revokeForPatient_rejectsWhenPersonalSaludTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long autorizacionId = 1L;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> autorizacionPacienteService.revokeForPatient(currentUser, autorizacionId));

        assertEquals("Solo un paciente puede revocar autorizaciones", ex.getMessage());
    }

    @Test
    void revokeForPatient_rejectsWhenPatientTriesToRevokeOtherPatientAuthorization() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long autorizacionId = 1L;

        AutorizacionPaciente autorizacion = new AutorizacionPaciente();
        autorizacion.setId(autorizacionId);
        autorizacion.setPacienteId(999L); // Different patient
        autorizacion.setPersonalId(5L);
        autorizacion.setEstado("ACTIVA");

        Paciente paciente = new Paciente();
        paciente.setId(10L); // Current user's patient
        paciente.setUsuarioId(1L);

        when(autorizacionPacienteRepository.findById(autorizacionId)).thenReturn(Optional.of(autorizacion));
        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> autorizacionPacienteService.revokeForPatient(currentUser, autorizacionId));

        assertEquals("No puedes revocar una autorización de un paciente que no es tuyo", ex.getMessage());
    }

    @Test
    void revokeForPatient_rejectsWhenAuthorizationNotActive() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long autorizacionId = 1L;

        AutorizacionPaciente autorizacion = new AutorizacionPaciente();
        autorizacion.setId(autorizacionId);
        autorizacion.setPacienteId(10L);
        autorizacion.setPersonalId(5L);
        autorizacion.setEstado("REVOCADA");

        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setUsuarioId(1L);

        when(autorizacionPacienteRepository.findById(autorizacionId)).thenReturn(Optional.of(autorizacion));
        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> autorizacionPacienteService.revokeForPatient(currentUser, autorizacionId));

        assertEquals("Solo se pueden revocar autorizaciones ACTIVAS", ex.getMessage());
    }
}