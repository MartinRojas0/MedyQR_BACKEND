package com.mediqr.backend.service;

import com.mediqr.backend.dto.RecordatorioMedicamentoCreateRequest;
import com.mediqr.backend.dto.RecordatorioMedicamentoUpdateRequest;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.RecordatorioMedicamento;
import com.mediqr.backend.repository.MedicamentoRepository;
import com.mediqr.backend.repository.RecordatorioMedicamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordatorioMedicamentoServiceTest {

    @Mock
    private RecordatorioMedicamentoRepository recordatorioRepository;

    @Mock
    private MedicamentoRepository medicamentoRepository;

    private RecordatorioMedicamentoService recordatorioService;

    @BeforeEach
    void setUp() {
        recordatorioService = new RecordatorioMedicamentoService(recordatorioRepository, medicamentoRepository);
    }

    @Test
    void createValidatesMedicationAndDefaultsActive() {
        RecordatorioMedicamentoCreateRequest request = createRequest(1L);
        when(medicamentoRepository.existsById(1L)).thenReturn(true);
        when(recordatorioRepository.save(any(RecordatorioMedicamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecordatorioMedicamento result = recordatorioService.create(request);

        assertEquals(1L, result.getMedicamentoId());
        assertEquals(LocalTime.of(8, 30), result.getHoraToma());
        assertEquals(true, result.getActivo());
        verify(recordatorioRepository).save(any(RecordatorioMedicamento.class));
    }

    @Test
    void createRejectsMissingMedication() {
        RecordatorioMedicamentoCreateRequest request = createRequest(999L);
        when(medicamentoRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> recordatorioService.create(request));
        verify(recordatorioRepository, never()).save(any());
    }

    @Test
    void getByMedicationRejectsMissingMedication() {
        when(medicamentoRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> recordatorioService.findByMedicamentoId(999L));
        verify(recordatorioRepository, never()).findByMedicamentoId(999L);
    }

    @Test
    void getUpdateAndDeleteRejectMissingReminder() {
        when(recordatorioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recordatorioService.getById(999L));
        assertThrows(ResourceNotFoundException.class,
                () -> recordatorioService.update(999L, new RecordatorioMedicamentoUpdateRequest()));
        assertThrows(ResourceNotFoundException.class, () -> recordatorioService.deleteById(999L));
        verify(recordatorioRepository, never()).deleteById(999L);
    }

    @Test
    void updateChangesEditableFieldsAndPreservesMedication() {
        RecordatorioMedicamento existing = new RecordatorioMedicamento();
        existing.setId(4L);
        existing.setMedicamentoId(1L);
        existing.setActivo(true);
        RecordatorioMedicamentoUpdateRequest request = new RecordatorioMedicamentoUpdateRequest();
        request.setHoraToma(LocalTime.of(20, 0));
        request.setMensaje("Actualizado");
        request.setActivo(false);
        when(recordatorioRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(recordatorioRepository.save(existing)).thenReturn(existing);

        RecordatorioMedicamento result = recordatorioService.update(4L, request);

        assertEquals(4L, result.getId());
        assertEquals(1L, result.getMedicamentoId());
        assertEquals(LocalTime.of(20, 0), result.getHoraToma());
        assertEquals(false, result.getActivo());
    }

    private RecordatorioMedicamentoCreateRequest createRequest(Long medicamentoId) {
        RecordatorioMedicamentoCreateRequest request = new RecordatorioMedicamentoCreateRequest();
        request.setMedicamentoId(medicamentoId);
        request.setHoraToma(LocalTime.of(8, 30));
        request.setMensaje("Tomar medicamento");
        return request;
    }
}
