package com.mediqr.backend.service;

import com.mediqr.backend.model.RegistroAcceso;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.RegistroAccesoRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroAccesoServiceTest {

    @Mock
    private RegistroAccesoRepository registroAccesoRepository;

    @Mock
    private PersonalSaludRepository personalSaludRepository;

    private RegistroAccesoService registroAccesoService;

    @BeforeEach
    void setUp() {
        registroAccesoService = new RegistroAccesoService(registroAccesoRepository, personalSaludRepository);
    }

    @Test
    void registrarAcceso_personalSalud_populatesPersonalId() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);
        Long pacienteId = 10L;
        String tipoAcceso = "CONSULTA";
        String accion = "LECTURA_HISTORIAL";
        UUID qrId = UUID.randomUUID();

        PersonalSalud personal = new PersonalSalud();
        personal.setId(5L);
        personal.setUsuarioId(2L);

        when(personalSaludRepository.findByUsuarioId(2L)).thenReturn(Optional.of(personal));
        when(registroAccesoRepository.save(any(RegistroAcceso.class))).thenAnswer(invocation -> {
            RegistroAcceso r = invocation.getArgument(0);
            r.setId(1L);
            r.setFechaAcceso(OffsetDateTime.now());
            return r;
        });

        RegistroAcceso response = registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, qrId);

        assertEquals(pacienteId, response.getPacienteId());
        assertEquals(5L, response.getPersonalId());
        assertEquals(tipoAcceso, response.getTipoAcceso());
        assertEquals(accion, response.getAccion());
        assertEquals(qrId, response.getIdentificadorQr());
        // fechaAcceso is set by service
        verify(registroAccesoRepository).save(any(RegistroAcceso.class));
    }

    @Test
    void registrarAcceso_paciente_personalIdNull() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        Long pacienteId = 10L;
        String tipoAcceso = "EMERGENCIA";
        String accion = "LECTURA_QR";

        when(registroAccesoRepository.save(any(RegistroAcceso.class))).thenAnswer(invocation -> {
            RegistroAcceso r = invocation.getArgument(0);
            r.setId(1L);
            r.setFechaAcceso(OffsetDateTime.now());
            return r;
        });

        RegistroAcceso response = registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);

        assertEquals(pacienteId, response.getPacienteId());
        assertNull(response.getPersonalId());
        assertEquals(tipoAcceso, response.getTipoAcceso());
        assertEquals(accion, response.getAccion());
        assertNull(response.getIdentificadorQr());
        verify(registroAccesoRepository).save(any(RegistroAcceso.class));
    }

    @Test
    void findByPacienteId_returnsList() {
        Long pacienteId = 10L;
        RegistroAcceso r1 = new RegistroAcceso();
        r1.setId(1L);
        r1.setPacienteId(pacienteId);
        RegistroAcceso r2 = new RegistroAcceso();
        r2.setId(2L);
        r2.setPacienteId(pacienteId);

        when(registroAccesoRepository.findByPacienteId(pacienteId)).thenReturn(List.of(r1, r2));

        List<RegistroAcceso> result = registroAccesoService.findByPacienteId(pacienteId);

        assertEquals(2, result.size());
    }

    @Test
    void findByPersonalId_returnsList() {
        Long personalId = 5L;
        RegistroAcceso r1 = new RegistroAcceso();
        r1.setId(1L);
        r1.setPersonalId(personalId);

        when(registroAccesoRepository.findByPersonalId(personalId)).thenReturn(List.of(r1));

        List<RegistroAcceso> result = registroAccesoService.findByPersonalId(personalId);

        assertEquals(1, result.size());
    }
}