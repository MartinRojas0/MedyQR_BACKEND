package com.mediqr.backend.service;

import com.mediqr.backend.dto.QrEmergenciaCreateRequest;
import com.mediqr.backend.dto.QrEmergenciaEmergencyResponse;
import com.mediqr.backend.dto.QrEmergenciaResponse;
import com.mediqr.backend.dto.QrEmergenciaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.QrEmergencia;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.HistorialClinico;
import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.model.RegistroAcceso;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.QrEmergenciaRepository;
import com.mediqr.backend.repository.HistorialClinicoRepository;
import com.mediqr.backend.repository.MedicamentoRepository;
import com.mediqr.backend.repository.RegistroAccesoRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import com.mediqr.backend.authorization.domain.AccessControlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

@Service
@Transactional
public class QrEmergenciaService {

    private final QrEmergenciaRepository qrEmergenciaRepository;
    private final PacienteRepository pacienteRepository;
    private final HistorialClinicoRepository historialClinicoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final RegistroAccesoRepository registroAccesoRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;

    public QrEmergenciaService(QrEmergenciaRepository qrEmergenciaRepository,
                               PacienteRepository pacienteRepository,
                               HistorialClinicoRepository historialClinicoRepository,
                               MedicamentoRepository medicamentoRepository,
                               RegistroAccesoRepository registroAccesoRepository,
                               RegistroAccesoService registroAccesoService,
                               CurrentUserService currentUserService,
                               AccessControlService accessControlService) {
        this.qrEmergenciaRepository = qrEmergenciaRepository;
        this.pacienteRepository = pacienteRepository;
        this.historialClinicoRepository = historialClinicoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.registroAccesoRepository = registroAccesoRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
        this.accessControlService = accessControlService;
    }

    public List<QrEmergenciaResponse> findAll() {
        return qrEmergenciaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<QrEmergenciaResponse> findById(Long id) {
        return qrEmergenciaRepository.findById(id).map(this::toResponse);
    }

    public List<QrEmergenciaResponse> findByPacienteId(Long pacienteId) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        if (!accessControlService.canAccessPatient(currentUser, pacienteId)) {
            throw new IllegalArgumentException("No tienes autorización para acceder a este paciente");
        }

        List<QrEmergenciaResponse> qrs = qrEmergenciaRepository.findByPacienteId(pacienteId).stream()
                .map(this::toResponse)
                .toList();

        if (!qrs.isEmpty()) {
            registrarAccesoGestion(pacienteId, "LECTURA_LISTA_QR");
        }

        return qrs;
    }

    public QrEmergenciaResponse getById(Long id) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        QrEmergencia qr = qrEmergenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        if (!accessControlService.canAccessPatient(currentUser, qr.getPacienteId())) {
            throw new IllegalArgumentException("No tienes autorización para acceder a este QR");
        }

        registrarAccesoGestion(qr.getPacienteId(), "LECTURA_QR");

        return toResponse(qr);
    }

    public QrEmergenciaResponse create(QrEmergenciaCreateRequest request) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        if (!"PACIENTE".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un paciente puede crear su QR de emergencia");
        }

        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado para el usuario autenticado"));

        Long pacienteId = paciente.getId();

        if (!request.getPacienteId().equals(pacienteId)) {
            throw new IllegalArgumentException("No puedes crear un QR para otro paciente");
        }

        if (qrEmergenciaRepository.existsByPacienteId(pacienteId)) {
            throw new BusinessRuleException("El paciente ya tiene un QR de emergencia");
        }

        QrEmergencia qr = new QrEmergencia();
        qr.setPacienteId(pacienteId);
        if (request.getMostrarDni() != null) qr.setMostrarDni(request.getMostrarDni());
        if (request.getMostrarTipoSangre() != null) qr.setMostrarTipoSangre(request.getMostrarTipoSangre());
        if (request.getMostrarAlergias() != null) qr.setMostrarAlergias(request.getMostrarAlergias());
        if (request.getMostrarMedicamentos() != null) qr.setMostrarMedicamentos(request.getMostrarMedicamentos());
        if (request.getMostrarEnfermedades() != null) qr.setMostrarEnfermedades(request.getMostrarEnfermedades());
        if (request.getActivo() != null) qr.setActivo(request.getActivo());

        QrEmergencia saved = qrEmergenciaRepository.save(qr);

        registrarAccesoGestion(saved.getPacienteId(), "CREACION_QR");

        return toResponse(saved);
    }

    public QrEmergenciaResponse update(Long id, QrEmergenciaUpdateRequest request) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        QrEmergencia qr = qrEmergenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        if (!"PACIENTE".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un paciente puede actualizar su QR de emergencia");
        }

        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        if (!qr.getPacienteId().equals(paciente.getId())) {
            throw new IllegalArgumentException("No puedes actualizar el QR de otro paciente");
        }

        if (request.getMostrarDni() != null) qr.setMostrarDni(request.getMostrarDni());
        if (request.getMostrarTipoSangre() != null) qr.setMostrarTipoSangre(request.getMostrarTipoSangre());
        if (request.getMostrarAlergias() != null) qr.setMostrarAlergias(request.getMostrarAlergias());
        if (request.getMostrarMedicamentos() != null) qr.setMostrarMedicamentos(request.getMostrarMedicamentos());
        if (request.getMostrarEnfermedades() != null) qr.setMostrarEnfermedades(request.getMostrarEnfermedades());
        if (request.getActivo() != null) qr.setActivo(request.getActivo());

        QrEmergencia saved = qrEmergenciaRepository.save(qr);

        registrarAccesoGestion(saved.getPacienteId(), "ACTUALIZACION_QR");

        return toResponse(saved);
    }

    public void deleteById(Long id) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        QrEmergencia qr = qrEmergenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        if (!"PACIENTE".equals(currentUser.rol())) {
            throw new IllegalArgumentException("Solo un paciente puede eliminar su QR de emergencia");
        }

        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        if (!qr.getPacienteId().equals(paciente.getId())) {
            throw new IllegalArgumentException("No puedes eliminar el QR de otro paciente");
        }

        Long pacienteId = qr.getPacienteId();
        qrEmergenciaRepository.deleteById(id);

        registrarAccesoGestion(pacienteId, "ELIMINACION_QR");
    }

    public QrEmergenciaEmergencyResponse accessByToken(UUID token) {
        QrEmergencia qr = qrEmergenciaRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        if (!qr.getActivo()) {
            throw new BusinessRuleException("El QR de emergencia no está activo");
        }

        Paciente paciente = pacienteRepository.findById(qr.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        HistorialClinico historial = historialClinicoRepository.findByPacienteId(qr.getPacienteId()).orElse(null);
        List<Medicamento> medicamentos = medicamentoRepository.findByPacienteId(qr.getPacienteId());

        registrarAccesoEmergencia(qr.getPacienteId(), token);

        return buildEmergencyResponse(paciente, historial, medicamentos, qr);
    }

    private QrEmergenciaResponse toResponse(QrEmergencia qr) {
        return new QrEmergenciaResponse(
                qr.getId(),
                qr.getPacienteId(),
                qr.getActivo(),
                qr.getMostrarDni(),
                qr.getMostrarTipoSangre(),
                qr.getMostrarAlergias(),
                qr.getMostrarMedicamentos(),
                qr.getMostrarEnfermedades(),
                qr.getCreatedAt(),
                qr.getUpdatedAt()
        );
    }

    private QrEmergenciaEmergencyResponse buildEmergencyResponse(Paciente paciente,
                                                                  HistorialClinico historial,
                                                                  List<Medicamento> medicamentos,
                                                                  QrEmergencia qr) {
        String medicamentosStr = medicamentos.stream()
                .filter(m -> m.getActivo())
                .map(m -> m.getNombre() + " " + m.getDosis() + " " + m.getFrecuencia() + (m.getInstrucciones() != null ? " - " + m.getInstrucciones() : ""))
                .reduce((a, b) -> a + "; " + b)
                .orElse("");

        String tipoSangre = historial != null ? historial.getTipoSangre() : "";
        String alergias = historial != null ? historial.getAlergias() : "";
        String enfermedades = historial != null ? historial.getEnfermedadesCronicas() : "";

        if (!qr.getMostrarDni()) {
            paciente.setDocumentoIdentidad(null);
        }
        if (!qr.getMostrarTipoSangre()) {
            tipoSangre = "";
        }
        if (!qr.getMostrarAlergias()) {
            alergias = "";
        }
        if (!qr.getMostrarEnfermedades()) {
            enfermedades = "";
        }
        if (!qr.getMostrarMedicamentos()) {
            medicamentosStr = "";
        }

        return new QrEmergenciaEmergencyResponse(
                paciente.getId(),
                paciente.getNombres(),
                paciente.getApellidos(),
                paciente.getDocumentoIdentidad(),
                paciente.getFechaNacimiento(),
                tipoSangre,
                alergias,
                enfermedades,
                medicamentosStr,
                OffsetDateTime.now()
        );
    }

    private void registrarAccesoGestion(Long pacienteId, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, "QR_EMERGENCIA", accion, UUID.randomUUID());
        }
    }

    private void registrarAccesoEmergencia(Long pacienteId, UUID qrToken) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, "QR_EMERGENCIA", "LECTURA_EMERGENCIA", qrToken);
        } else {
            // Acceso anónimo mediante QR - registrar sin usuario autenticado
            // Creamos un registro mínimo sin personal_id
            // Nota: RegistroAccesoService.registrarAcceso requiere CurrentUser, así que usamos un enfoque diferente
            // Para acceso anónimo, creamos el registro directamente
            RegistroAcceso registro = new RegistroAcceso();
            registro.setPacienteId(pacienteId);
            registro.setPersonalId(null);
            registro.setTipoAcceso("QR_EMERGENCIA");
            registro.setAccion("LECTURA_EMERGENCIA");
            registro.setFechaAcceso(OffsetDateTime.now());
            registro.setIdentificadorQr(qrToken);
            registroAccesoRepository.save(registro);
        }
    }
}