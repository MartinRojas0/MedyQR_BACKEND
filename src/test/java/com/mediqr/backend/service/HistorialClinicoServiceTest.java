package com.mediqr.backend.service;

import com.mediqr.backend.dto.HistorialClinicoCreateRequest;
import com.mediqr.backend.dto.HistorialClinicoUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.HistorialClinico;
import com.mediqr.backend.repository.HistorialClinicoRepository;
import com.mediqr.backend.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialClinicoServiceTest {

    @Mock
    private HistorialClinicoRepository historialClinicoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    private HistorialClinicoService historialClinicoService;

    @BeforeEach
    void setUp() {
        historialClinicoService = new HistorialClinicoService(
                historialClinicoRepository, pacienteRepository);
    }

    @Test
    void createValidatesPatientAndMapsClinicalFields() {
        HistorialClinicoCreateRequest request = createRequest(10L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(historialClinicoRepository.existsByPacienteId(10L)).thenReturn(false);
        when(historialClinicoRepository.save(any(HistorialClinico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HistorialClinico result = historialClinicoService.create(request);

        assertEquals(10L, result.getPacienteId());
        assertEquals("O+", result.getTipoSangre());
        assertEquals("Ninguna", result.getAlergias());
        verify(historialClinicoRepository).save(any(HistorialClinico.class));
    }

    @Test
    void createRejectsMissingPatient() {
        HistorialClinicoCreateRequest request = createRequest(10L);
        when(pacienteRepository.existsById(10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> historialClinicoService.create(request));
        verify(historialClinicoRepository, never()).existsByPacienteId(any());
        verify(historialClinicoRepository, never()).save(any());
    }

    @Test
    void createRejectsSecondHistoryForSamePatient() {
        HistorialClinicoCreateRequest request = createRequest(10L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(historialClinicoRepository.existsByPacienteId(10L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> historialClinicoService.create(request));
        verify(historialClinicoRepository, never()).save(any());
    }

    @Test
    void getUpdateAndDeleteRejectMissingHistory() {
        when(historialClinicoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> historialClinicoService.getById(999L));
        assertThrows(ResourceNotFoundException.class,
                () -> historialClinicoService.update(999L, new HistorialClinicoUpdateRequest()));
        assertThrows(ResourceNotFoundException.class, () -> historialClinicoService.deleteById(999L));
        verify(historialClinicoRepository, never()).deleteById(999L);
    }

    @Test
    void updateChangesOnlyClinicalFields() {
        HistorialClinico existing = new HistorialClinico();
        existing.setId(4L);
        existing.setPacienteId(10L);
        HistorialClinicoUpdateRequest request = new HistorialClinicoUpdateRequest();
        request.setTipoSangre("A+");
        request.setAlergias("Polen");
        request.setEnfermedadesCronicas("Ninguna");
        request.setCirugias("Ninguna");
        request.setObservaciones("Actualizado");
        when(historialClinicoRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(historialClinicoRepository.save(existing)).thenReturn(existing);

        HistorialClinico result = historialClinicoService.update(4L, request);

        assertEquals(4L, result.getId());
        assertEquals(10L, result.getPacienteId());
        assertEquals("A+", result.getTipoSangre());
        assertEquals("Polen", result.getAlergias());
    }

    private HistorialClinicoCreateRequest createRequest(Long pacienteId) {
        HistorialClinicoCreateRequest request = new HistorialClinicoCreateRequest();
        request.setPacienteId(pacienteId);
        request.setTipoSangre("O+");
        request.setAlergias("Ninguna");
        request.setEnfermedadesCronicas("Ninguna");
        request.setCirugias("Ninguna");
        request.setObservaciones("Inicial");
        return request;
    }
}
