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
class ParticipanteJuntaServiceTest {

    @Mock
    private ParticipanteJuntaRepository participanteJuntaRepository;

    @Mock
    private JuntaMedicaRepository juntaMedicaRepository;

    @Mock
    private PersonalSaludRepository personalSaludRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    private ParticipanteJuntaService participanteJuntaService;

    @BeforeEach
    void setUp() {
        participanteJuntaService = new ParticipanteJuntaService(
                participanteJuntaRepository, juntaMedicaRepository, personalSaludRepository, registroAccesoService, currentUserService);
    }

    @Test
    void addParticipantSucceedsWhenCreatorAdds() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;
        Long personalId = 6L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        PersonalSalud participante = new PersonalSalud();
        participante.setId(personalId);
        participante.setNombres("Dra. Ana");
        participante.setApellidos("Gomez");

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setPacienteId(10L);
        junta.setCreadorId(5L);
        junta.setEstado("ACTIVA");
        junta.setFechaInicio(OffsetDateTime.now().minusHours(1));
        junta.setFechaExpiracion(OffsetDateTime.now().plusDays(7));

        ParticipanteJuntaCreateRequest request = new ParticipanteJuntaCreateRequest();
        request.setPersonalId(personalId);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));
        when(personalSaludRepository.findById(personalId)).thenReturn(Optional.of(participante));
        when(participanteJuntaRepository.existsByJuntaIdAndPersonalId(juntaId, personalId)).thenReturn(false);
        when(participanteJuntaRepository.save(any(ParticipanteJunta.class))).thenAnswer(invocation -> {
            ParticipanteJunta p = invocation.getArgument(0);
            p.setId(1L);
            p.setFechaIngreso(OffsetDateTime.now());
            return p;
        });

        ParticipanteJuntaResponse response = participanteJuntaService.addParticipant(juntaId, request);

        assertEquals(personalId, response.getPersonalId());
        assertEquals("Dra. Ana", response.getPersonalNombres());
        assertEquals("Gomez", response.getPersonalApellidos());
        verify(participanteJuntaRepository).save(any(ParticipanteJunta.class));
        verify(registroAccesoService).registrarAcceso(any(CurrentUserService.CurrentUser.class), anyLong(), anyString(), anyString(), any());
    }

    @Test
    void addParticipantRejectsWhenPacienteTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long juntaId = 1L;

        ParticipanteJuntaCreateRequest request = new ParticipanteJuntaCreateRequest();
        request.setPersonalId(6L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertThrows(IllegalArgumentException.class,
                () -> participanteJuntaService.addParticipant(juntaId, request));
    }

    @Test
    void addParticipantRejectsWhenNonCreatorTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(3L, "otro@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        PersonalSalud otro = new PersonalSalud();
        otro.setId(6L);
        otro.setUsuarioId(3L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(1L);
        junta.setCreadorId(5L);
        junta.setEstado("ACTIVA");
        junta.setFechaInicio(OffsetDateTime.now().minusHours(1));
        junta.setFechaExpiracion(OffsetDateTime.now().plusDays(7));

        ParticipanteJuntaCreateRequest request = new ParticipanteJuntaCreateRequest();
        request.setPersonalId(7L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(3L)).thenReturn(Optional.of(otro));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));

        assertThrows(IllegalArgumentException.class,
                () -> participanteJuntaService.addParticipant(juntaId, request));
    }

    @Test
    void addParticipantRejectsWhenJuntaNotActive() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setCreadorId(5L);
        junta.setEstado("CERRADA"); // Not active

        ParticipanteJuntaCreateRequest request = new ParticipanteJuntaCreateRequest();
        request.setPersonalId(6L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));

        assertThrows(BusinessRuleException.class,
                () -> participanteJuntaService.addParticipant(juntaId, request));
    }

    @Test
    void addParticipantRejectsWhenPersonalAlreadyParticipant() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;
        Long personalId = 6L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        PersonalSalud participante = new PersonalSalud();
        participante.setId(personalId);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setCreadorId(5L);
        junta.setEstado("ACTIVA");
        junta.setFechaInicio(OffsetDateTime.now().minusHours(1));
        junta.setFechaExpiracion(OffsetDateTime.now().plusDays(7));

        ParticipanteJuntaCreateRequest request = new ParticipanteJuntaCreateRequest();
        request.setPersonalId(personalId);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));
        when(personalSaludRepository.findById(personalId)).thenReturn(Optional.of(participante));
        when(participanteJuntaRepository.existsByJuntaIdAndPersonalId(juntaId, personalId)).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> participanteJuntaService.addParticipant(juntaId, request));
    }

    @Test
    void addParticipantRejectsWhenAddingCreatorAsParticipant() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        PersonalSalud participante = new PersonalSalud();
        participante.setId(5L); // Same as creator

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setCreadorId(5L); // Same as creator
        junta.setEstado("ACTIVA");
        junta.setFechaInicio(OffsetDateTime.now().minusHours(1));
        junta.setFechaExpiracion(OffsetDateTime.now().plusDays(7));

        ParticipanteJuntaCreateRequest request = new ParticipanteJuntaCreateRequest();
        request.setPersonalId(5L); // Trying to add creator

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));
        when(personalSaludRepository.findById(5L)).thenReturn(Optional.of(participante));

        assertThrows(BusinessRuleException.class,
                () -> participanteJuntaService.addParticipant(juntaId, request));
    }

    @Test
    void removeParticipantSucceedsWhenCreatorRemoves() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;
        Long personalId = 6L;

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setPacienteId(10L);
        junta.setCreadorId(5L);

        ParticipanteJunta participante = new ParticipanteJunta();
        participante.setId(1L);
        participante.setJuntaId(juntaId);
        participante.setPersonalId(personalId);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));
        when(participanteJuntaRepository.findByJuntaIdAndPersonalId(juntaId, personalId)).thenReturn(Optional.of(participante));

        participanteJuntaService.removeParticipant(juntaId, personalId);

        verify(participanteJuntaRepository).delete(participante);
        verify(registroAccesoService).registrarAcceso(any(CurrentUserService.CurrentUser.class), anyLong(), anyString(), anyString(), any());
    }

    @Test
    void removeParticipantRejectsWhenTryingToRemoveCreator() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long juntaId = 1L;
        Long personalId = 5L; // Same as creator

        PersonalSalud creador = new PersonalSalud();
        creador.setId(5L);
        creador.setUsuarioId(2L);

        JuntaMedica junta = new JuntaMedica();
        junta.setId(juntaId);
        junta.setCreadorId(5L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(creador));
        when(juntaMedicaRepository.findById(juntaId)).thenReturn(Optional.of(junta));

        assertThrows(BusinessRuleException.class,
                () -> participanteJuntaService.removeParticipant(juntaId, personalId));
    }
}