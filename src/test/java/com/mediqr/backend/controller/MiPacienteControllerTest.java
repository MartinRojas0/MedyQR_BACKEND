package com.mediqr.backend.controller;

import com.mediqr.backend.dto.PacienteUpdateRequest;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.PacienteService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MiPacienteControllerTest {

    @Test
    void updateMeOnlyUpdatesThePatientResolvedFromTheAuthenticatedUser() {
        PacienteService pacienteService = mock(PacienteService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        MiPacienteController controller = new MiPacienteController(pacienteService, currentUserService);
        Paciente ownPatient = new Paciente();
        ownPatient.setId(42L);
        PacienteUpdateRequest request = new PacienteUpdateRequest();
        Paciente updatedPatient = new Paciente();
        updatedPatient.setId(42L);

        when(currentUserService.getCurrentUserId()).thenReturn(7L);
        when(pacienteService.findByUsuarioId(7L)).thenReturn(Optional.of(ownPatient));
        when(pacienteService.update(42L, request)).thenReturn(updatedPatient);

        var response = controller.updateMe(request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(updatedPatient, response.getBody());
        verify(pacienteService).findByUsuarioId(7L);
        verify(pacienteService).update(42L, request);
    }

    @Test
    void updateMeDoesNotUpdateWhenTheAuthenticatedUserHasNoPatient() {
        PacienteService pacienteService = mock(PacienteService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        MiPacienteController controller = new MiPacienteController(pacienteService, currentUserService);
        PacienteUpdateRequest request = new PacienteUpdateRequest();

        when(currentUserService.getCurrentUserId()).thenReturn(7L);
        when(pacienteService.findByUsuarioId(7L)).thenReturn(Optional.empty());

        var response = controller.updateMe(request);

        assertEquals(404, response.getStatusCode().value());
        verify(pacienteService, never()).update(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }
}
