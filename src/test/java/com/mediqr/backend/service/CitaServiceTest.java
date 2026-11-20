package com.mediqr.backend.service;

import com.mediqr.backend.dto.CitaCreateRequest;
import com.mediqr.backend.dto.CitaEstadoRequest;
import com.mediqr.backend.dto.CitaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Cita;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.CitaRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

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
    private UsuarioRepository usuarioRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    private CitaService citaService;

    @BeforeEach
    void setUp() {
        citaService = new CitaService(
                citaRepository, pacienteRepository, personalSaludRepository,
                usuarioRepository, registroAccesoService, currentUserService);
    }

    @Test
    void createBuildsPendingAppointmentWhenReferencesAndSlotAreAvailable() {
        CitaCreateRequest request = createRequest(10L, 20L);
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(activePatient()));
        stubActiveProfessional(20L);
        when(citaRepository.existsByPersonalIdAndFechaHoraAndEstadoIn(
                20L, request.getFechaHora(), Set.of("PENDIENTE", "CONFIRMADA"))).thenReturn(false);
        when(citaRepository.existsByPacienteIdAndFechaHoraAndEstadoIn(
                10L, request.getFechaHora(), Set.of("PENDIENTE", "CONFIRMADA"))).thenReturn(false);
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
        when(pacienteRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> citaService.create(request));
        verify(personalSaludRepository, never()).findById(any());
        verify(citaRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingHealthProfessional() {
        CitaCreateRequest request = createRequest(10L, 20L);
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(activePatient()));
        when(personalSaludRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> citaService.create(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void createRejectsOccupiedProfessionalSlot() {
        CitaCreateRequest request = createRequest(10L, 20L);
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(activePatient()));
        stubActiveProfessional(20L);
        when(citaRepository.existsByPersonalIdAndFechaHoraAndEstadoIn(
                20L, request.getFechaHora(), Set.of("PENDIENTE", "CONFIRMADA"))).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> citaService.create(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void availabilityReportsOccupiedAndFreeProfessionalSlots() {
        OffsetDateTime dateTime = futureDate(30, 10);
        stubActiveProfessional(20L);
        when(citaRepository.existsByPersonalIdAndFechaHoraAndEstadoIn(
                20L, dateTime, Set.of("PENDIENTE", "CONFIRMADA"))).thenReturn(false, true);

        assertTrue(citaService.isAvailable(20L, dateTime));
        assertFalse(citaService.isAvailable(20L, dateTime));
    }

    @Test
    void updateChangesOnlyEditableAppointmentFields() {
        Cita existing = new Cita();
        existing.setId(30L);
        existing.setPacienteId(10L);
        existing.setPersonalId(20L);
        existing.setFechaHora(futureDate(30, 10));
        CitaUpdateRequest request = new CitaUpdateRequest();
        request.setPersonalId(21L);
        request.setFechaHora(futureDate(31, 11));
        request.setMotivo("Actualizada");
        when(citaRepository.findById(30L)).thenReturn(java.util.Optional.of(existing));
        stubActiveProfessional(21L);
        when(citaRepository.existsByPersonalIdAndFechaHoraAndEstadoInAndIdNot(
                21L, request.getFechaHora(), Set.of("PENDIENTE", "CONFIRMADA"), 30L)).thenReturn(false);
        when(citaRepository.existsByPacienteIdAndFechaHoraAndEstadoInAndIdNot(
                10L, request.getFechaHora(), Set.of("PENDIENTE", "CONFIRMADA"), 30L)).thenReturn(false);
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
        request.setFechaHora(futureDate(30, 10));
        request.setMotivo("Consulta");
        return request;
    }

    private Paciente activePatient() {
        Paciente patient = new Paciente();
        patient.setId(10L);
        patient.setActivo(true);
        return patient;
    }

    private void stubActiveProfessional(Long id) {
        PersonalSalud professional = new PersonalSalud();
        professional.setId(id);
        professional.setUsuarioId(200L + id);
        professional.setActivo(true);
        when(personalSaludRepository.findById(id)).thenReturn(Optional.of(professional));

        Usuario user = new Usuario();
        user.setId(200L + id);
        user.setRol("PERSONAL_SALUD");
        user.setActivo(true);
        when(usuarioRepository.findById(200L + id)).thenReturn(Optional.of(user));
    }

    private OffsetDateTime futureDate(int days, int hour) {
        return OffsetDateTime.now().plusDays(days).withHour(hour).withMinute(0).withSecond(0).withNano(0);
    }
}