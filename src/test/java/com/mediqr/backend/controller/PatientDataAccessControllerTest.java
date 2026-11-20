package com.mediqr.backend.controller;

import com.mediqr.backend.authorization.domain.PatientAccessGuard;
import com.mediqr.backend.authorization.domain.AccessControlService;
import com.mediqr.backend.dto.CitaEstadoRequest;
import com.mediqr.backend.dto.ConsultaMedicaUpdateRequest;
import com.mediqr.backend.dto.HistorialClinicoCreateRequest;
import com.mediqr.backend.dto.MedicamentoCreateRequest;
import com.mediqr.backend.dto.RecordatorioMedicamentoCreateRequest;
import com.mediqr.backend.service.CitaService;
import com.mediqr.backend.service.ConsultaMedicaService;
import com.mediqr.backend.service.HistorialClinicoService;
import com.mediqr.backend.service.MedicamentoService;
import com.mediqr.backend.service.RecordatorioMedicamentoService;
import com.mediqr.backend.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class PatientDataAccessControllerTest {

    @Test
    void deniedCreateCannotWriteHistorialForAnotherPatient() {
        HistorialClinicoService service = mock(HistorialClinicoService.class);
        PatientAccessGuard guard = deniedGuardFor(42L);
        HistorialClinicoController controller = new HistorialClinicoController(service, guard);
        HistorialClinicoCreateRequest request = new HistorialClinicoCreateRequest();
        request.setPacienteId(42L);

        assertThrows(AccessDeniedException.class, () -> controller.create(request));

        verify(service, never()).create(any(HistorialClinicoCreateRequest.class));
    }

    @Test
    void deniedStatusChangeCannotMutateAnotherPatientsAppointment() {
        CitaService service = mock(CitaService.class);
        PatientAccessGuard guard = deniedGuardFor(42L);
        CitaController controller = new CitaController(service, guard);
        when(service.findPacienteIdById(9L)).thenReturn(42L);

        assertThrows(AccessDeniedException.class, () -> controller.updateEstado(9L, new CitaEstadoRequest()));

        verify(service, never()).updateEstado(org.mockito.ArgumentMatchers.eq(9L), any(CitaEstadoRequest.class));
    }

    @Test
    void deniedUpdateCannotMutateAnotherPatientsConsultation() {
        ConsultaMedicaService service = mock(ConsultaMedicaService.class);
        PatientAccessGuard guard = deniedGuardFor(42L);
        ConsultaMedicaController controller = new ConsultaMedicaController(service, guard);
        when(service.findPacienteIdById(9L)).thenReturn(42L);

        assertThrows(AccessDeniedException.class,
                () -> controller.update(9L, new ConsultaMedicaUpdateRequest()));

        verify(service, never()).update(org.mockito.ArgumentMatchers.eq(9L),
                any(ConsultaMedicaUpdateRequest.class));
    }

    @Test
    void deniedCreateCannotWriteMedicationForAnotherPatient() {
        MedicamentoService service = mock(MedicamentoService.class);
        PatientAccessGuard guard = deniedGuardFor(42L);
        MedicamentoController controller = new MedicamentoController(service, guard);
        MedicamentoCreateRequest request = new MedicamentoCreateRequest();
        request.setPacienteId(42L);

        assertThrows(AccessDeniedException.class, () -> controller.create(request));

        verify(service, never()).create(any(MedicamentoCreateRequest.class));
    }

    @Test
    void deniedDeleteCannotRemoveAnotherPatientsMedication() {
        MedicamentoService service = mock(MedicamentoService.class);
        PatientAccessGuard guard = deniedGuardFor(42L);
        MedicamentoController controller = new MedicamentoController(service, guard);
        when(service.findPacienteIdById(9L)).thenReturn(42L);

        assertThrows(AccessDeniedException.class, () -> controller.delete(9L));

        verify(service, never()).deleteById(9L);
    }

    @Test
    void deniedCreateCannotWriteReminderForAnotherPatientsMedication() {
        RecordatorioMedicamentoService service = mock(RecordatorioMedicamentoService.class);
        PatientAccessGuard guard = deniedGuardFor(42L);
        RecordatorioMedicamentoController controller = new RecordatorioMedicamentoController(service, guard);
        RecordatorioMedicamentoCreateRequest request = new RecordatorioMedicamentoCreateRequest();
        request.setMedicamentoId(7L);
        when(service.findPacienteIdByMedicamentoId(7L)).thenReturn(42L);

        assertThrows(AccessDeniedException.class, () -> controller.create(request));

        verify(service, never()).create(any(RecordatorioMedicamentoCreateRequest.class));
    }

    @Test
    void globalRecordListsAreDeniedWithoutCallingFindAll() {
        HistorialClinicoService historialService = mock(HistorialClinicoService.class);
        CitaService citaService = mock(CitaService.class);
        ConsultaMedicaService consultaService = mock(ConsultaMedicaService.class);
        MedicamentoService medicamentoService = mock(MedicamentoService.class);
        RecordatorioMedicamentoService recordatorioService = mock(RecordatorioMedicamentoService.class);
        PatientAccessGuard guard = mock(PatientAccessGuard.class);

        assertThrows(AccessDeniedException.class,
                () -> new HistorialClinicoController(historialService, guard).getAll());
        assertThrows(AccessDeniedException.class, () -> new CitaController(citaService, guard).getAll());
        assertThrows(AccessDeniedException.class,
                () -> new ConsultaMedicaController(consultaService, guard).getAll());
        assertThrows(AccessDeniedException.class,
                () -> new MedicamentoController(medicamentoService, guard).getAll());
        assertThrows(AccessDeniedException.class,
                () -> new RecordatorioMedicamentoController(recordatorioService, guard).getAll());

        verify(historialService, never()).findAll();
        verify(citaService, never()).findAll();
        verify(consultaService, never()).findAll();
        verify(medicamentoService, never()).findAll();
        verify(recordatorioService, never()).findAll();
    }

    private PatientAccessGuard deniedGuardFor(Long pacienteId) {
        AccessControlService accessControlService = mock(AccessControlService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        var professional = new CurrentUserService.CurrentUser(17L, "professional@example.test",
                "PERSONAL_SALUD", true);
        when(currentUserService.getCurrentUser()).thenReturn(professional);
        when(accessControlService.canAccessPatient(professional, pacienteId)).thenReturn(false);
        return new PatientAccessGuard(accessControlService, currentUserService);
    }
}
