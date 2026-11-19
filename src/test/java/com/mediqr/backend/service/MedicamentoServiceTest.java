package com.mediqr.backend.service;

import com.mediqr.backend.dto.MedicamentoCreateRequest;
import com.mediqr.backend.dto.MedicamentoUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.repository.MedicamentoRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicamentoServiceTest {

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    private MedicamentoService medicamentoService;

    @BeforeEach
    void setUp() {
        medicamentoService = new MedicamentoService(medicamentoRepository, pacienteRepository, registroAccesoService, currentUserService);
    }

    @Test
    void createValidatesPatientAndDefaultsActive() {
        MedicamentoCreateRequest request = createRequest(10L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(medicamentoRepository.save(any(Medicamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Medicamento result = medicamentoService.create(request);

        assertEquals(10L, result.getPacienteId());
        assertEquals("Medicamento", result.getNombre());
        assertEquals(true, result.getActivo());
        verify(medicamentoRepository).save(any(Medicamento.class));
    }

    @Test
    void createRejectsMissingPatient() {
        MedicamentoCreateRequest request = createRequest(10L);
        when(pacienteRepository.existsById(10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> medicamentoService.create(request));
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    void createRejectsDatesOutsideDatabaseCheck() {
        MedicamentoCreateRequest request = createRequest(10L);
        request.setFechaInicio(LocalDate.of(2026, 10, 2));
        request.setFechaFin(LocalDate.of(2026, 10, 1));
        when(pacienteRepository.existsById(10L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> medicamentoService.create(request));
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    void getByPatientRejectsMissingPatient() {
        when(pacienteRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> medicamentoService.findByPacienteId(999L));
        verify(medicamentoRepository, never()).findByPacienteId(999L);
    }

    @Test
    void getUpdateAndDeleteRejectMissingMedication() {
        when(medicamentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> medicamentoService.getById(999L));
        assertThrows(ResourceNotFoundException.class,
                () -> medicamentoService.update(999L, new MedicamentoUpdateRequest()));
        assertThrows(ResourceNotFoundException.class, () -> medicamentoService.deleteById(999L));
        verify(medicamentoRepository, never()).deleteById(999L);
    }

    @Test
    void updateChangesEditableFieldsAndPreservesPatient() {
        Medicamento existing = new Medicamento();
        existing.setId(4L);
        existing.setPacienteId(10L);
        existing.setActivo(true);
        MedicamentoUpdateRequest request = new MedicamentoUpdateRequest();
        request.setNombre("Medicamento actualizado");
        request.setDosis("10 mg");
        request.setFechaInicio(LocalDate.of(2026, 10, 1));
        request.setFechaFin(LocalDate.of(2026, 10, 10));
        request.setActivo(false);
        when(medicamentoRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(medicamentoRepository.save(existing)).thenReturn(existing);

        Medicamento result = medicamentoService.update(4L, request);

        assertEquals(4L, result.getId());
        assertEquals(10L, result.getPacienteId());
        assertEquals("Medicamento actualizado", result.getNombre());
        assertEquals(false, result.getActivo());
    }

    private MedicamentoCreateRequest createRequest(Long pacienteId) {
        MedicamentoCreateRequest request = new MedicamentoCreateRequest();
        request.setPacienteId(pacienteId);
        request.setNombre("Medicamento");
        request.setDosis("5 mg");
        request.setFrecuencia("Diaria");
        request.setFechaInicio(LocalDate.of(2026, 10, 1));
        request.setFechaFin(LocalDate.of(2026, 10, 10));
        request.setInstrucciones("Tomar por la mañana");
        return request;
    }
}