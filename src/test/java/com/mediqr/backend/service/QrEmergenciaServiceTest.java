package com.mediqr.backend.service;

import com.mediqr.backend.dto.QrEmergenciaCreateRequest;
import com.mediqr.backend.dto.QrEmergenciaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.QrEmergencia;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.HistorialClinico;
import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.QrEmergenciaRepository;
import com.mediqr.backend.repository.HistorialClinicoRepository;
import com.mediqr.backend.repository.MedicamentoRepository;
import com.mediqr.backend.repository.RegistroAccesoRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import com.mediqr.backend.authorization.domain.AccessControlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QrEmergenciaServiceTest {

    @Mock
    private QrEmergenciaRepository qrRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private HistorialClinicoRepository historialClinicoRepository;

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private RegistroAccesoRepository registroAccesoRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private QrEmergenciaService qrService;

    private CurrentUserService.CurrentUser pacienteUser;
    private CurrentUserService.CurrentUser doctorUser;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        pacienteUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        doctorUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);

        paciente = new Paciente();
        paciente.setId(10L);
        paciente.setUsuarioId(1L);
        paciente.setNombres("Juan");
        paciente.setApellidos("Perez");
        paciente.setDocumentoIdentidad("12345678");
        paciente.setFechaNacimiento(java.time.LocalDate.of(1990, 1, 1));

        // Default lenient stubbing for all common dependencies
        lenient().when(currentUserService.getCurrentUser()).thenReturn(pacienteUser);
        lenient().when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));
        lenient().when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        lenient().when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));
        lenient().when(pacienteRepository.existsById(10L)).thenReturn(true);
        lenient().when(qrRepository.existsByPacienteId(10L)).thenReturn(false);
        lenient().when(accessControlService.canAccessPatient(pacienteUser, 10L)).thenReturn(true);
        lenient().when(pacienteRepository.findByUsuarioId(2L)).thenReturn(Optional.of(paciente));
    }

    @Test
    void createValidatesPatientGeneratesTokenAndUsesDefaults() {
        QrEmergenciaCreateRequest request = new QrEmergenciaCreateRequest();
        request.setPacienteId(10L);

        when(qrRepository.existsByPacienteId(10L)).thenReturn(false);
        when(qrRepository.save(any(QrEmergencia.class)))
                .thenAnswer(invocation -> {
                    QrEmergencia q = invocation.getArgument(0);
                    q.setId(1L);
                    q.setToken(UUID.randomUUID());
                    return q;
                });

        var result = qrService.create(request);

        assertEquals(10L, result.getPacienteId());
        assertNotNull(result.getId());
        assertEquals(true, result.getMostrarDni());
        assertEquals(true, result.getActivo());
        verify(qrRepository).save(any(QrEmergencia.class));
    }

    @Test
    void createRejectsMissingPatient() {
        QrEmergenciaCreateRequest request = new QrEmergenciaCreateRequest();
        request.setPacienteId(999L);

        when(currentUserService.getCurrentUser()).thenReturn(pacienteUser);
        // pacienteRepository.existsById(999L) is NOT called by service (ownership check fails first)
        // No stubbing needed for existsById

        // Service checks ownership first (999L != authenticated user's pacienteId=10L) -> throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> qrService.create(request));
        verify(qrRepository, never()).existsByPacienteId(any());
        verify(qrRepository, never()).save(any());
    }

    @Test
    void createRejectsSecondQrForSamePatient() {
        QrEmergenciaCreateRequest request = new QrEmergenciaCreateRequest();
        request.setPacienteId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(pacienteUser);
        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));
        // pacienteRepository.existsById(10L) already stubbed in @BeforeEach with lenient
        when(qrRepository.existsByPacienteId(10L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> qrService.create(request));
        verify(qrRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenPacienteTriesToCreateForAnotherPatient() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(1L, "paciente@test.com", "PACIENTE", true);
        
        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setUsuarioId(1L);

        QrEmergenciaCreateRequest request = new QrEmergenciaCreateRequest();
        request.setPacienteId(20L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));

        assertThrows(IllegalArgumentException.class, () -> qrService.create(request));
    }

    @Test
    void createRejectsWhenNonPacienteTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "doctor@test.com", "PERSONAL_SALUD", true);

        QrEmergenciaCreateRequest request = new QrEmergenciaCreateRequest();
        request.setPacienteId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertThrows(IllegalArgumentException.class, () -> qrService.create(request));
    }

    @Test
    void getUpdateAndDeleteRejectMissingQr() {
        lenient().when(accessControlService.canAccessPatient(pacienteUser, 999L)).thenReturn(true);
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

        when(currentUserService.getCurrentUser()).thenReturn(pacienteUser);
        when(pacienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(paciente));
        when(qrRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(qrRepository.save(existing)).thenReturn(existing);

        var result = qrService.update(4L, request);

        assertEquals(4L, result.getId());
        assertEquals(10L, result.getPacienteId());
        assertEquals(false, result.getMostrarDni());
        assertEquals(false, result.getActivo());
        verify(qrRepository).save(existing);
    }

    @Test
    void updateRejectsWhenNonOwnerTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "otro@test.com", "PACIENTE", true);
        
        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setUsuarioId(1L);

        UUID token = UUID.randomUUID();
        QrEmergencia existing = new QrEmergencia();
        existing.setId(4L);
        existing.setPacienteId(20L); // QR belongs to DIFFERENT patient (20L), not the user's patient (10L)
        existing.setToken(token);

        QrEmergenciaUpdateRequest request = new QrEmergenciaUpdateRequest();
        request.setMostrarDni(false);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(pacienteRepository.findByUsuarioId(2L)).thenReturn(Optional.of(paciente));
        when(qrRepository.findById(4L)).thenReturn(Optional.of(existing));
        // accessControlService.canAccessPatient is NOT called because ownership check fails first

        assertThrows(IllegalArgumentException.class, () -> qrService.update(4L, request));
    }

    @Test
    void deleteRejectsWhenNonOwnerTries() {
        CurrentUserService.CurrentUser currentUser = new CurrentUserService.CurrentUser(2L, "otro@test.com", "PACIENTE", true);
        
        Paciente paciente = new Paciente();
        paciente.setId(20L); // Different from QR's pacienteId (10L)
        paciente.setUsuarioId(2L);

        QrEmergencia existing = new QrEmergencia();
        existing.setId(4L);
        existing.setPacienteId(10L); // QR belongs to patient 10L

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(pacienteRepository.findByUsuarioId(2L)).thenReturn(Optional.of(paciente));
        when(qrRepository.findById(4L)).thenReturn(Optional.of(existing));
        // Service doesn't call accessControlService in deleteById

        assertThrows(IllegalArgumentException.class, () -> qrService.deleteById(4L));
    }

    @Test
    void accessByTokenReturnsEmergencyInfo() {
        UUID token = UUID.randomUUID();
        QrEmergencia qr = new QrEmergencia();
        qr.setId(1L);
        qr.setPacienteId(10L);
        qr.setToken(token);
        qr.setActivo(true);
        qr.setMostrarDni(true);
        qr.setMostrarTipoSangre(true);
        qr.setMostrarAlergias(true);
        qr.setMostrarMedicamentos(true);
        qr.setMostrarEnfermedades(true);

        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setNombres("Juan");
        paciente.setApellidos("Perez");
        paciente.setDocumentoIdentidad("12345678");
        paciente.setFechaNacimiento(java.time.LocalDate.of(1990, 1, 1));

        when(currentUserService.getCurrentUser()).thenReturn(pacienteUser);
        when(qrRepository.findByToken(token)).thenReturn(Optional.of(qr));
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        when(historialClinicoRepository.findByPacienteId(10L)).thenReturn(Optional.empty());
        when(medicamentoRepository.findByPacienteId(10L)).thenReturn(List.of());

        var response = qrService.accessByToken(token);

        assertEquals(10L, response.getPacienteId());
        assertEquals("Juan", response.getNombres());
        assertEquals("Perez", response.getApellidos());
        assertEquals("12345678", response.getDocumentoIdentidad());
        verify(registroAccesoService).registrarAcceso(any(), anyLong(), anyString(), anyString(), any());
    }

    @Test
    void accessByTokenRespectsVisibilityFlags() {
        UUID token = UUID.randomUUID();
        QrEmergencia qr = new QrEmergencia();
        qr.setId(1L);
        qr.setPacienteId(10L);
        qr.setToken(token);
        qr.setActivo(true);
        qr.setMostrarDni(false);
        qr.setMostrarTipoSangre(false);
        qr.setMostrarAlergias(false);
        qr.setMostrarMedicamentos(false);
        qr.setMostrarEnfermedades(false);

        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setNombres("Juan");
        paciente.setApellidos("Perez");
        paciente.setDocumentoIdentidad("12345678");

        when(qrRepository.findByToken(token)).thenReturn(Optional.of(qr));
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        when(historialClinicoRepository.findByPacienteId(10L)).thenReturn(Optional.empty());
        when(medicamentoRepository.findByPacienteId(10L)).thenReturn(List.of());

        var response = qrService.accessByToken(token);

        assertEquals(10L, response.getPacienteId());
        assertEquals(null, response.getDocumentoIdentidad());
        assertEquals("", response.getTipoSangre());
        assertEquals("", response.getAlergias());
        assertEquals("", response.getEnfermedadesCronicas());
        assertEquals("", response.getMedicamentos());
    }

    @Test
    void accessByTokenRejectsInactiveQr() {
        UUID token = UUID.randomUUID();
        QrEmergencia qr = new QrEmergencia();
        qr.setId(1L);
        qr.setPacienteId(10L);
        qr.setToken(token);
        qr.setActivo(false);

        when(qrRepository.findByToken(token)).thenReturn(Optional.of(qr));

        assertThrows(BusinessRuleException.class, () -> qrService.accessByToken(token));
    }

    @Test
    void accessByTokenRejectsMissingQr() {
        UUID token = UUID.randomUUID();
        when(qrRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> qrService.accessByToken(token));
    }
}