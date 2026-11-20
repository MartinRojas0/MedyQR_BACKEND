package com.mediqr.backend.service;

import com.mediqr.backend.dto.QrEmergenciaCreateRequest;
import com.mediqr.backend.dto.QrEmergenciaEmergencyResponse;
import com.mediqr.backend.dto.QrEmergenciaResponse;
import com.mediqr.backend.dto.QrEmergenciaUpdateRequest;
import com.mediqr.backend.dto.QrFichaPublicaResponse;
import com.mediqr.backend.dto.QrIssueRequest;
import com.mediqr.backend.dto.QrIssueResponse;
import com.mediqr.backend.dto.QrPublicTokenRequest;
import com.mediqr.backend.dto.QrStatusResponse;
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
import org.springframework.security.access.AccessDeniedException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
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
    private final SecureRandom secureRandom = new SecureRandom();

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
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (!accessControlService.canAccessPatient(currentUser, pacienteId)) {
            throw new AccessDeniedException("No tienes autorización para acceder a este paciente");
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
            throw new AccessDeniedException("Usuario no autenticado");
        }

        QrEmergencia qr = qrEmergenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        if (!accessControlService.canAccessPatient(currentUser, qr.getPacienteId())) {
            throw new AccessDeniedException("No tienes autorización para acceder a este QR");
        }

        registrarAccesoGestion(qr.getPacienteId(), "LECTURA_QR");

        return toResponse(qr);
    }

    public QrEmergenciaResponse create(QrEmergenciaCreateRequest request) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (!"PACIENTE".equals(currentUser.rol())) {
            throw new AccessDeniedException("Solo un paciente puede crear su QR de emergencia");
        }

        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado para el usuario autenticado"));

        Long pacienteId = paciente.getId();

        if (!request.getPacienteId().equals(pacienteId)) {
            throw new AccessDeniedException("No puedes crear un QR para otro paciente");
        }

        if (qrEmergenciaRepository.existsByPacienteId(pacienteId)) {
            throw new BusinessRuleException("El paciente ya tiene un QR de emergencia");
        }

        QrEmergencia qr = new QrEmergencia();
        qr.setPacienteId(pacienteId);
        qr.setToken(UUID.randomUUID());
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

    public QrIssueResponse issueForCurrentPatient(QrIssueRequest request, String frontendBaseUrl) {
        var currentUser = requirePatientUser();
        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        OffsetDateTime now = OffsetDateTime.now();

        qrEmergenciaRepository.revokeActiveForPatient(paciente.getId(), now);

        String token = generateToken();
        QrEmergencia qr = new QrEmergencia();
        qr.setPacienteId(paciente.getId());
        qr.setToken(null);
        qr.setTokenHash(hashToken(token));
        qr.setExpiresAt(now.plusDays(30));
        qr.setRevokedAt(null);
        qr.setActivo(true);
        qr.setMostrarDni(false);
        qr.setMostrarTipoSangre(Boolean.TRUE.equals(request.mostrarTipoSangre()));
        qr.setMostrarAlergias(Boolean.TRUE.equals(request.mostrarAlergias()));
        qr.setMostrarMedicamentos(Boolean.TRUE.equals(request.mostrarMedicamentos()));
        qr.setMostrarEnfermedades(Boolean.TRUE.equals(request.mostrarEnfermedades()));
        qrEmergenciaRepository.save(qr);

        registrarAccesoGestion(paciente.getId(), "CREACION_QR");
        return new QrIssueResponse(token, buildPublicUrl(frontendBaseUrl, token), qr.getExpiresAt());
    }

    public QrStatusResponse statusForCurrentPatient() {
        var currentUser = requirePatientUser();
        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        QrEmergencia latest = qrEmergenciaRepository.findByPacienteId(paciente.getId()).stream()
                .filter(qr -> qr.getTokenHash() != null)
                .max(java.util.Comparator.comparing(
                        QrEmergencia::getCreatedAt,
                        java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())))
                .orElse(null);
        boolean active = latest != null
                && Boolean.TRUE.equals(latest.getActivo())
                && latest.getRevokedAt() == null
                && latest.getExpiresAt() != null
                && latest.getExpiresAt().isAfter(OffsetDateTime.now());
        return new QrStatusResponse(active, latest == null ? null : latest.getExpiresAt());
    }

    public void revokeForCurrentPatient() {
        var currentUser = requirePatientUser();
        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        qrEmergenciaRepository.revokeActiveForPatient(paciente.getId(), OffsetDateTime.now());
        registrarAccesoGestion(paciente.getId(), "REVOCACION_QR");
    }

    public byte[] generateImageForCurrentPatient(String token, String frontendBaseUrl) {
        var currentUser = requirePatientUser();
        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        QrEmergencia qr = findValidQr(token);
        if (!qr.getPacienteId().equals(paciente.getId())) {
            throw new AccessDeniedException("No puedes descargar el QR de otro paciente");
        }
        return generatePng(buildPublicUrl(frontendBaseUrl, token));
    }

    public PublicResolution resolvePublicToken(QrPublicTokenRequest request) {
        String tokenHash;
        try {
            tokenHash = hashToken(request.token());
        } catch (ResourceNotFoundException exception) {
            return new PublicResolution("invalid", null);
        }

        QrEmergencia qr = qrEmergenciaRepository.findByTokenHash(tokenHash).orElse(null);
        if (qr == null) {
            return new PublicResolution("invalid", null);
        }
        if (qr.getRevokedAt() != null || !Boolean.TRUE.equals(qr.getActivo())) {
            return new PublicResolution("revoked", null);
        }
        if (qr.getExpiresAt() == null || !qr.getExpiresAt().isAfter(OffsetDateTime.now())) {
            return new PublicResolution("expired", null);
        }

        Paciente paciente = pacienteRepository.findById(qr.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        HistorialClinico historial = historialClinicoRepository.findByPacienteId(qr.getPacienteId()).orElse(null);
        List<Medicamento> medicamentos = medicamentoRepository.findByPacienteId(qr.getPacienteId());

        String tipoSangre = Boolean.TRUE.equals(qr.getMostrarTipoSangre()) && historial != null
                ? historial.getTipoSangre() : null;
        String alergias = Boolean.TRUE.equals(qr.getMostrarAlergias()) && historial != null
                ? historial.getAlergias() : null;
        String enfermedades = Boolean.TRUE.equals(qr.getMostrarEnfermedades()) && historial != null
                ? historial.getEnfermedadesCronicas() : null;
        List<String> nombresMedicamentos = Boolean.TRUE.equals(qr.getMostrarMedicamentos())
                ? medicamentos.stream()
                    .filter(medicamento -> Boolean.TRUE.equals(medicamento.getActivo()))
                    .filter(medicamento -> medicamento.getNombre() != null && !medicamento.getNombre().isBlank())
                    .limit(10)
                    .map(medicamento -> {
                        String dosis = medicamento.getDosis();
                        return dosis == null || dosis.isBlank()
                                ? medicamento.getNombre()
                                : medicamento.getNombre() + " (" + dosis + ")";
                    })
                    .toList()
                : List.of();

        boolean hasMedicalInformation = hasText(tipoSangre) || hasText(alergias)
                || hasText(enfermedades) || !nombresMedicamentos.isEmpty();
        QrFichaPublicaResponse response = new QrFichaPublicaResponse(
                tipoSangre,
                alergias,
                enfermedades,
                nombresMedicamentos,
                hasMedicalInformation);
        registrarAccesoPublico(paciente.getId());
        return new PublicResolution("valid", response);
    }

    public record PublicResolution(String state, QrFichaPublicaResponse response) {
    }

    public QrEmergenciaResponse update(Long id, QrEmergenciaUpdateRequest request) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        QrEmergencia qr = qrEmergenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        if (!"PACIENTE".equals(currentUser.rol())) {
            throw new AccessDeniedException("Solo un paciente puede actualizar su QR de emergencia");
        }

        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        if (!qr.getPacienteId().equals(paciente.getId())) {
            throw new AccessDeniedException("No puedes actualizar el QR de otro paciente");
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
            throw new AccessDeniedException("Usuario no autenticado");
        }

        QrEmergencia qr = qrEmergenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        if (!"PACIENTE".equals(currentUser.rol())) {
            throw new AccessDeniedException("Solo un paciente puede eliminar su QR de emergencia");
        }

        Paciente paciente = pacienteRepository.findByUsuarioId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        if (!qr.getPacienteId().equals(paciente.getId())) {
            throw new AccessDeniedException("No puedes eliminar el QR de otro paciente");
        }

        Long pacienteId = qr.getPacienteId();
        qrEmergenciaRepository.deleteById(id);

        registrarAccesoGestion(pacienteId, "ELIMINACION_QR");
    }

    public QrEmergenciaEmergencyResponse accessByToken(UUID token) {
        QrEmergencia qr = qrEmergenciaRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !accessControlService.canAccessPatient(currentUser, qr.getPacienteId())) {
            throw new AccessDeniedException("No tienes autorización para acceder a este paciente");
        }

        if (!Boolean.TRUE.equals(qr.getActivo()) || qr.getRevokedAt() != null
                || qr.getExpiresAt() == null || !qr.getExpiresAt().isAfter(OffsetDateTime.now())) {
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

        String documentoIdentidad = Boolean.TRUE.equals(qr.getMostrarDni())
                ? paciente.getDocumentoIdentidad() : null;
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
                documentoIdentidad,
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

        private CurrentUserService.CurrentUser requirePatientUser() {
            var currentUser = currentUserService.getCurrentUser();
            if (currentUser == null || !"PACIENTE".equals(currentUser.rol())) {
                throw new AccessDeniedException("Solo el paciente autenticado puede gestionar su QR");
            }
            return currentUser;
        }

        private QrEmergencia findValidQr(String token) {
            String hash = hashToken(token);
            QrEmergencia qr = qrEmergenciaRepository.findByTokenHash(hash)
                    .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));
            if (!Boolean.TRUE.equals(qr.getActivo()) || qr.getRevokedAt() != null
                    || qr.getExpiresAt() == null || !qr.getExpiresAt().isAfter(OffsetDateTime.now())) {
                throw new BusinessRuleException("El QR de emergencia no está activo");
            }
            return qr;
        }

        private String generateToken() {
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }

        private String hashToken(String token) {
            if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
                throw new ResourceNotFoundException("QR de emergencia no encontrado");
            }
            byte[] decoded;
            try {
                decoded = Base64.getUrlDecoder().decode(token);
            } catch (IllegalArgumentException exception) {
                throw new ResourceNotFoundException("QR de emergencia no encontrado");
            }
            if (decoded.length != 32
                    || !Base64.getUrlEncoder().withoutPadding().encodeToString(decoded).equals(token)) {
                throw new ResourceNotFoundException("QR de emergencia no encontrado");
            }
            try {
                return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(decoded));
            } catch (NoSuchAlgorithmException exception) {
                throw new IllegalStateException("SHA-256 is unavailable", exception);
            }
        }

        private String buildPublicUrl(String frontendBaseUrl, String token) {
            URI uri;
            try {
                uri = URI.create(frontendBaseUrl);
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("The configured frontend URL is invalid", exception);
            }
            boolean localHttp = "http".equalsIgnoreCase(uri.getScheme())
                    && ("localhost".equalsIgnoreCase(uri.getHost()) || "127.0.0.1".equals(uri.getHost()));
            if (uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                    || (uri.getPath() != null && !uri.getPath().isEmpty() && !"/".equals(uri.getPath()))
                    || (!"https".equalsIgnoreCase(uri.getScheme()) && !localHttp)) {
                throw new IllegalStateException("The configured frontend URL must be an HTTPS origin");
            }
            String origin = frontendBaseUrl.replaceAll("/+$", "");
            return origin + "/#/ficha/" + token;
        }

        private byte[] generatePng(String publicUrl) {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 2);
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                var matrix = new QRCodeWriter().encode(publicUrl, BarcodeFormat.QR_CODE, 400, 400, hints);
                MatrixToImageWriter.writeToStream(matrix, "PNG", output);
                return output.toByteArray();
            } catch (WriterException | IOException exception) {
                throw new IllegalStateException("Could not generate the emergency QR image", exception);
            }
        }

        private void registrarAccesoPublico(Long pacienteId) {
            RegistroAcceso registro = new RegistroAcceso();
            registro.setPacienteId(pacienteId);
            registro.setPersonalId(null);
            registro.setTipoAcceso("QR_EMERGENCIA");
            registro.setAccion("LECTURA_EMERGENCIA_PUBLICA");
            registro.setFechaAcceso(OffsetDateTime.now());
            registro.setIdentificadorQr(null);
            registroAccesoRepository.save(registro);
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
}