package com.mediqr.backend.service;

import com.mediqr.backend.dto.QrEmergenciaCreateRequest;
import com.mediqr.backend.dto.QrEmergenciaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.QrEmergencia;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.QrEmergenciaRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrEmergenciaServiceTest {

    @Mock
    private QrEmergenciaRepository qrRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    private QrEmergenciaService qrService;

    @BeforeEach
    void setUp() {
        qrService = new QrEmergenciaService(qrRepository, pacienteRepository, registroAccesoService, currentUserService);
    }

    @Test
    void createValidatesPatientGeneratesTokenAndUsesDefaults() {
        QrEmergenciaCreateRequest request = new QrEmergenciaCreateRequest();
        request.setPacienteId(10L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(qrRepository.existsByPacienteId(10L)).thenReturn(false);
        when(qrRepository.save(any(QrEmergencia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QrEmergencia result = qrService.create(request);

        assertEquals(10L, result.getPacienteId());
        assertNotNull(result.getToken());
        assertEquals(true, result.getMostrarDni());
        assertEquals(true, result.getActivo());
        verify(qrRepository).save(any(QrEmergencia.class));
    }

    @Test
    void createRejectsMissingPatient() {
        QrEmergenciaCreateRequest request = new QrEmergenciaCreateRequest();
        request.setPacienteId(999L);
        when(pacienteRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> qrService.create(request));
        verify(qrRepository, never()).existsByPacienteId(any());
        verify(qrRepository, never()).save(any());
    }

    @Test
    void createRejectsSecondQrForSamePatient() {
        QrEmergenciaCreateRequest request = new QrEmergenciaCreateRequest();
        request.setPacienteId(10L);
        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(qrRepository.existsByPacienteId(10L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> qrService.create(request));
        verify(qrRepository, never()).save(any());
    }

    @Test
    void getUpdateAndDeleteRejectMissingQr() {
        when(qrRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> qrService.getById(999L));
        assertThrows(ResourceNotFoundException.class,
                () -> qrService.update(999L, new QrEmergenciaUpdateRequest()));
        assertThrows(ResourceNotFoundException.class, () -> qrService.deleteById(999L));
        verify(qrRepository, never()).deleteById(999L);
    }

    @Test
    void updateChangesVisibilityOnlyAndPreservesPatientAndToken() {
        UUID token = UUID.randomUUID();
        QrEmergencia existing = new QrEmergencia();
        existing.setId(4L);
        existing.setPacienteId(10L);
        existing.setToken(token);
        QrEmergenciaUpdateRequest request = new QrEmergenciaUpdateRequest();
        request.setMostrarDni(false);
        request.setMostrarAlergias(false);
        request.setActivo(false);
        when(qrRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(qrRepository.save(existing)).thenReturn(existing);

        QrEmergencia result = qrService.update(4L, request);

        assertEquals(4L, result.getId());
        assertEquals(10L, result.getPacienteId());
        assertEquals(token, result.getToken());
        assertEquals(false, result.getMostrarDni());
        assertEquals(false, result.getActivo());
    }
}