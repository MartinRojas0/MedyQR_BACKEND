package com.mediqr.backend.service;

import com.mediqr.backend.dto.ConsultaMedicaCreateRequest;
import com.mediqr.backend.dto.ConsultaMedicaUpdateRequest;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.ConsultaMedica;
import com.mediqr.backend.repository.ConsultaMedicaRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaMedicaServiceTest {

    @Mock
    private ConsultaMedicaRepository consultaMedicaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PersonalSaludRepository personalSaludRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    private ConsultaMedicaService consultaMedicaService;

    @BeforeEach
    void setUp() {
        consultaMedicaService = new ConsultaMedicaService(
                consultaMedicaRepository, pacienteRepository, personalSaludRepository, registroAccesoService, currentUserService);
    }

    @Test
    void createValidatesReferencesAndMapsClinicalFields() {
        ConsultaMedicaCreateRequest request = createRequest(10L, 1L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(personalSaludRepository.existsById(1L)).thenReturn(true);
        when(consultaMedicaRepository.save(any(ConsultaMedica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaMedica result = consultaMedicaService.create(request);

        assertEquals(10L, result.getPacienteId());
        assertEquals(1L, result.getPersonalId());
        assertEquals(request.getFechaConsulta(), result.getFechaConsulta());
        assertEquals("Diagnóstico", result.getDiagnostico());
        verify(consultaMedicaRepository).save(any(ConsultaMedica.class));
    }

    @Test
    void createRejectsMissingPatient() {
        ConsultaMedicaCreateRequest request = createRequest(10L, 1L);
        when(pacienteRepository.existsById(10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> consultaMedicaService.create(request));
        verify(personalSaludRepository, never()).existsById(any());
        verify(consultaMedicaRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingHealthProfessional() {
        ConsultaMedicaCreateRequest request = createRequest(10L, 1L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(personalSaludRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> consultaMedicaService.create(request));
        verify(consultaMedicaRepository, never()).save(any());
    }

    @Test
    void getUpdateAndDeleteRejectMissingConsultation() {
        when(consultaMedicaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> consultaMedicaService.getById(999L));
        assertThrows(ResourceNotFoundException.class,
                () -> consultaMedicaService.update(999L, new ConsultaMedicaUpdateRequest()));
        assertThrows(ResourceNotFoundException.class, () -> consultaMedicaService.deleteById(999L));
        verify(consultaMedicaRepository, never()).deleteById(999L);
    }

    @Test
    void updateChangesOnlyClinicalFields() {
        ConsultaMedica existing = new ConsultaMedica();
        existing.setId(4L);
        existing.setPacienteId(10L);
        existing.setPersonalId(1L);
        existing.setFechaConsulta(OffsetDateTime.parse("2026-10-01T10:00:00Z"));
        ConsultaMedicaUpdateRequest request = new ConsultaMedicaUpdateRequest();
        request.setMotivo("Seguimiento");
        request.setDiagnostico("Diagnóstico actualizado");
        request.setTratamiento("Tratamiento actualizado");
        request.setObservaciones("Observación actualizada");
        when(consultaMedicaRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(consultaMedicaRepository.save(existing)).thenReturn(existing);

        ConsultaMedica result = consultaMedicaService.update(4L, request);

        assertEquals(4L, result.getId());
        assertEquals(10L, result.getPacienteId());
        assertEquals(1L, result.getPersonalId());
        assertEquals("Diagnóstico actualizado", result.getDiagnostico());
        assertEquals(existing.getFechaConsulta(), result.getFechaConsulta());
    }

    private ConsultaMedicaCreateRequest createRequest(Long pacienteId, Long personalId) {
        ConsultaMedicaCreateRequest request = new ConsultaMedicaCreateRequest();
        request.setPacienteId(pacienteId);
        request.setPersonalId(personalId);
        request.setFechaConsulta(OffsetDateTime.parse("2026-10-01T10:00:00Z"));
        request.setMotivo("Consulta");
        request.setDiagnostico("Diagnóstico");
        request.setTratamiento("Tratamiento");
        return request;
    }
}