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
import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JuntaMedicaServiceTest {

    @Mock
    private JuntaMedicaRepository juntaMedicaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PersonalSaludRepository personalSaludRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    private JuntaMedicaService juntaMedicaService;

    @BeforeEach
    void setUp() {
        juntaMedicaService = new JuntaMedicaService(
                juntaMedicaRepository, pacienteRepository, personalSaludRepository, registroAccesoService, currentUserService);
    }

    @Test
    void createSucceedsWhenPersonalSaludCreatesValidJunta() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long pacienteId = 10L;

        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setUsuarioId(1L);

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedicaCreateRequest request = new JuntaMedicaCreateRequest();
        request.setPacienteId(pacienteId);
        request.setMotivo("Evaluación compleja");
        request.setFechaInicio(OffsetDateTime.now().plusHours(1));
        request.setFechaExpiracion(OffsetDateTime.now().plusDays(7));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.save(any(JuntaMedica.class))).thenAnswer(invocation -> {
            JuntaMedica j = invocation.getArgument(0);
            j.setId(1L);
            j.setEstado("ACTIVA");
            return j;
        });

        JuntaMedicaResponse response = juntaMedicaService.create(request);

        assertEquals("ACTIVA", response.getEstado());
        assertEquals(pacienteId, response.getPacienteId());
        assertEquals(5L, response.getCreadorId());
        assertEquals("Evaluación compleja", response.getMotivo());
        verify(juntaMedicaRepository).save(any(JuntaMedica.class));
        verify(registroAccesoService).registrarAcceso(any(CurrentUserService.CurrentUser.class), anyLong(), anyString(), anyString(), any());
    }

    @Test
    void createRejectsWhenPacienteTriesToCreate() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);

        JuntaMedicaCreateRequest request = new JuntaMedicaCreateRequest();
        request.setPacienteId(10L);
        request.setMotivo("Test");
        request.setFechaInicio(OffsetDateTime.now().plusHours(1));
        request.setFechaExpiracion(OffsetDateTime.now().plusDays(7));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> juntaMedicaService.create(request));

        assertEquals("Solo un profesional de salud puede crear una junta médica", ex.getMessage());
    }

    @Test
    void createRejectsWhenPatientDoesNotExist() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long pacienteId = 999L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedicaCreateRequest request = new JuntaMedicaCreateRequest();
        request.setPacienteId(pacienteId);
        request.setMotivo("Test");
        request.setFechaInicio(OffsetDateTime.now().plusHours(1));
        request.setFechaExpiracion(OffsetDateTime.now().plusDays(7));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> juntaMedicaService.create(request));
    }

    @Test
    void createRejectsWhenFechaInicioAfterFechaExpiracion() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long pacienteId = 10L;

        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedicaCreateRequest request = new JuntaMedicaCreateRequest();
        request.setPacienteId(pacienteId);
        request.setMotivo("Test");
        request.setFechaInicio(OffsetDateTime.now().plusDays(7));
        request.setFechaExpiracion(OffsetDateTime.now().plusHours(1)); // Before inicio

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));

        assertThrows(BusinessRuleException.class, () -> juntaMedicaService.create(request));
    }

    @Test
    void createRejectsWhenFechaExpiracionInPast() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long pacienteId = 10L;

        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedicaCreateRequest request = new JuntaMedicaCreateRequest();
        request.setPacienteId(pacienteId);
        request.setMotivo("Test");
        request.setFechaInicio(OffsetDateTime.now().minusDays(2));
        request.setFechaExpiracion(OffsetDateTime.now().minusDays(1)); // In the past

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));

        assertThrows(BusinessRuleException.class, () -> juntaMedicaService.create(request));
    }

    @Test
    void updateSucceedsWhenCreatorUpdates() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setPacienteId(10L);
        junta.setCreadorId(5L);
        junta.setMotivo("Original");
        junta.setFechaInicio(OffsetDateTime.now().plusHours(1));
        junta.setFechaExpiracion(OffsetDateTime.now().plusDays(7));
        junta.setEstado("ACTIVA");

        JuntaMedicaUpdateRequest request = new JuntaMedicaUpdateRequest();
        request.setMotivo("Actualizado");

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));
        when(juntaMedicaRepository.save(any(JuntaMedica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JuntaMedicaResponse response = juntaMedicaService.update(juntaId, request);

        assertEquals("Actualizado", response.getMotivo());
        verify(juntaMedicaRepository).save(any(JuntaMedica.class));
        verify(registroAccesoService).registrarAcceso(any(CurrentUserService.CurrentUser.class), anyLong(), anyString(), anyString(), any());
    }

    @Test
    void updateRejectsWhenNonCreatorTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(3L, "otro@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L); // Different from current user (3L)

        PersonalSalud otro = new PersonalSalud();
        otro.setId(6L);
        otro.setUsuarioId(3L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(1L);
        junta.setCreadorId(5L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(3L)).thenReturn(Optional.of(otro));
        when(juntaMedicaRepository.findById(1L)).thenReturn(Optional.of(junta));

        assertThrows(IllegalArgumentException.class, () -> juntaMedicaService.update(juntaId, new JuntaMedicaUpdateRequest()));
    }

    @Test
    void closeJuntaSucceedsWhenCreatorCloses() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setPacienteId(10L);
        junta.setCreadorId(5L);
        junta.setEstado("ACTIVA");

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));
        when(juntaMedicaRepository.save(any(JuntaMedica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JuntaMedicaResponse response = juntaMedicaService.closeJunta(juntaId);

        assertEquals("CERRADA", response.getEstado());
        verify(juntaMedicaRepository).save(any(JuntaMedica.class));
        verify(registroAccesoService).registrarAcceso(any(CurrentUserService.CurrentUser.class), anyLong(), anyString(), anyString(), any());
    }

    @Test
    void closeJuntaRejectsWhenNotActive() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setCreadorId(5L);
        junta.setEstado("CERRADA"); // Already closed

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));

        assertThrows(BusinessRuleException.class, () -> juntaMedicaService.closeJunta(juntaId));
    }

    @Test
    void deleteRejectsWhenNonCreatorTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(3L, "otro@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        PersonalSalud otro = new PersonalSalud();
        otro.setId(6L);
        otro.setUsuarioId(3L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setCreadorId(5L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(3L)).thenReturn(Optional.of(otro));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));

        assertThrows(IllegalArgumentException.class, () -> juntaMedicaService.deleteById(juntaId));
    }
}