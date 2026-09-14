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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QrEmergenciaService {

    private final QrEmergenciaRepository qrEmergenciaRepository;
    private final PacienteRepository pacienteRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    public QrEmergenciaService(QrEmergenciaRepository qrEmergenciaRepository,
                               PacienteRepository pacienteRepository,
                               RegistroAccesoService registroAccesoService,
                               CurrentUserService currentUserService) {
        this.qrEmergenciaRepository = qrEmergenciaRepository;
        this.pacienteRepository = pacienteRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<QrEmergencia> findAll() {
        return qrEmergenciaRepository.findAll();
    }

    public Optional<QrEmergencia> findById(Long id) {
        return qrEmergenciaRepository.findById(id);
    }

    public List<QrEmergencia> findByPacienteId(Long pacienteId) {
        validatePaciente(pacienteId);
        
        List<QrEmergencia> qrs = qrEmergenciaRepository.findByPacienteId(pacienteId);
        
        if (!qrs.isEmpty()) {
            registrarAcceso(pacienteId, "QR_EMERGENCIA", "LECTURA_LISTA_QR");
        }

        return qrs;
    }

    public QrEmergencia getById(Long id) {
        QrEmergencia qr = qrEmergenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));

        registrarAcceso(qr.getPacienteId(), "QR_EMERGENCIA", "LECTURA_QR");

        return qr;
    }

    public QrEmergencia create(QrEmergenciaCreateRequest request) {
        validatePaciente(request.getPacienteId());
        if (qrEmergenciaRepository.existsByPacienteId(request.getPacienteId())) {
            throw new BusinessRuleException("El paciente ya tiene un QR de emergencia");
        }

        QrEmergencia qr = new QrEmergencia();
        qr.setPacienteId(request.getPacienteId());
        if (request.getMostrarDni() != null) qr.setMostrarDni(request.getMostrarDni());
        if (request.getMostrarTipoSangre() != null) qr.setMostrarTipoSangre(request.getMostrarTipoSangre());
        if (request.getMostrarAlergias() != null) qr.setMostrarAlergias(request.getMostrarAlergias());
        if (request.getMostrarMedicamentos() != null) qr.setMostrarMedicamentos(request.getMostrarMedicamentos());
        if (request.getMostrarEnfermedades() != null) qr.setMostrarEnfermedades(request.getMostrarEnfermedades());
        if (request.getActivo() != null) qr.setActivo(request.getActivo());
        
        QrEmergencia saved = qrEmergenciaRepository.save(qr);
        
        registrarAcceso(saved.getPacienteId(), "QR_EMERGENCIA", "CREACION_QR");

        return saved;
    }

    public QrEmergencia update(Long id, QrEmergenciaUpdateRequest request) {
        QrEmergencia qr = getById(id);
        if (request.getMostrarDni() != null) qr.setMostrarDni(request.getMostrarDni());
        if (request.getMostrarTipoSangre() != null) qr.setMostrarTipoSangre(request.getMostrarTipoSangre());
        if (request.getMostrarAlergias() != null) qr.setMostrarAlergias(request.getMostrarAlergias());
        if (request.getMostrarMedicamentos() != null) qr.setMostrarMedicamentos(request.getMostrarMedicamentos());
        if (request.getMostrarEnfermedades() != null) qr.setMostrarEnfermedades(request.getMostrarEnfermedades());
        if (request.getActivo() != null) qr.setActivo(request.getActivo());
        
        QrEmergencia saved = qrEmergenciaRepository.save(qr);
        
        registrarAcceso(saved.getPacienteId(), "QR_EMERGENCIA", "ACTUALIZACION_QR");

        return saved;
    }

    public void deleteById(Long id) {
        QrEmergencia qr = getById(id);
        Long pacienteId = qr.getPacienteId();
        qrEmergenciaRepository.deleteById(id);
        
        registrarAcceso(pacienteId, "QR_EMERGENCIA", "ELIMINACION_QR");
    }

    private void validatePaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
    }

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            UUID qrToken = UUID.randomUUID();
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, qrToken);
        }
    }
}