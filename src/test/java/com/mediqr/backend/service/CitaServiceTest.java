package com.mediqr.backend.service;

import com.mediqr.backend.dto.CitaCreateRequest;
import com.mediqr.backend.dto.CitaEstadoRequest;
import com.mediqr.backend.dto.CitaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Cita;
import com.mediqr.backend.repository.CitaRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PersonalSaludRepository personalSaludRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    private CitaService citaService;

    @BeforeEach
    void setUp() {
        citaService = new CitaService(citaRepository, pacienteRepository, personalSaludRepository, registroAccesoService, currentUserService);
    }

    @Test
    void createBuildsPendingAppointmentWhenReferencesAndSlotAreAvailable() {
        CitaCreateRequest request = createRequest(10L, 20L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(personalSaludRepository.existsById(20L)).thenReturn(true);
        when(citaRepository.existsByPersonalIdAndFechaHora(20L, request.getFechaHora())).thenReturn(false);
        when(citaRepository.existsByPacienteIdAndFechaHora(10L, request.getFechaHora())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cita result = citaService.create(request);

        assertEquals(10L, result.getPacienteId());
        assertEquals(20L, result.getPersonalId());
        assertEquals("PENDIENTE", result.getEstado());
        verify(citaRepository).save(any(Cita.class));
    }

    @Test
    void createRejectsMissingPatient() {
        CitaCreateRequest request = createRequest(10L, 20L);
        when(pacienteRepository.existsById(10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> citaService.create(request));
        verify(personalSaludRepository, never()).existsById(any());
        verify(citaRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingHealthProfessional() {
        CitaCreateRequest request = createRequest(10L, 20L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(personalSaludRepository.existsById(20L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> citaService.create(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void createRejectsOccupiedProfessionalSlot() {
        CitaCreateRequest request = createRequest(10L, 20L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(personalSaludRepository.existsById(20L)).thenReturn(true);
        when(citaRepository.existsByPersonalIdAndFechaHora(20L, request.getFechaHora())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> citaService.create(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void availabilityReportsOccupiedAndFreeProfessionalSlots() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2026-10-01T10:00:00Z");
        when(personalSaludRepository.existsById(20L)).thenReturn(true);
        when(citaRepository.existsByPersonalIdAndFechaHora(20L, dateTime)).thenReturn(false, true);

        assertTrue(citaService.isAvailable(20L, dateTime));
        assertFalse(citaService.isAvailable(20L, dateTime));
    }

    @Test
    void updateChangesOnlyEditableAppointmentFields() {
        Cita existing = new Cita();
        existing.setId(30L);
        existing.setPacienteId(10L);
        existing.setPersonalId(20L);
        existing.setFechaHora(OffsetDateTime.parse("2026-10-01T10:00:00Z"));
        CitaUpdateRequest request = new CitaUpdateRequest();
        request.setPersonalId(21L);
        request.setFechaHora(OffsetDateTime.parse("2026-10-01T11:00:00Z"));
        request.setMotivo("Actualizada");
        when(citaRepository.findById(30L)).thenReturn(java.util.Optional.of(existing));
        when(personalSaludRepository.existsById(21L)).thenReturn(true);
        when(citaRepository.existsByPersonalIdAndFechaHoraAndIdNot(21L, request.getFechaHora(), 30L)).thenReturn(false);
        when(citaRepository.existsByPacienteIdAndFechaHoraAndIdNot(10L, request.getFechaHora(), 30L)).thenReturn(false);
        when(citaRepository.save(existing)).thenReturn(existing);

        Cita result = citaService.update(30L, request);

        assertEquals(30L, result.getId());
        assertEquals(10L, result.getPacienteId());
        assertEquals(21L, result.getPersonalId());
        assertEquals("Actualizada", result.getMotivo());
    }

    @Test
    void updateStateRejectsStateOutsideDatabaseCheck() {
        CitaEstadoRequest request = new CitaEstadoRequest();
        request.setEstado("INVALIDA");
        when(citaRepository.findById(30L)).thenReturn(java.util.Optional.of(new Cita()));

        assertThrows(BusinessRuleException.class, () -> citaService.updateEstado(30L, request));
        verify(citaRepository, never()).save(any());
    }

    private CitaCreateRequest createRequest(Long pacienteId, Long personalId) {
        CitaCreateRequest request = new CitaCreateRequest();
        request.setPacienteId(pacienteId);
        request.setPersonalId(personalId);
        request.setFechaHora(OffsetDateTime.parse("2026-10-01T10:00:00Z"));
        request.setMotivo("Consulta");
        return request;
    }
}