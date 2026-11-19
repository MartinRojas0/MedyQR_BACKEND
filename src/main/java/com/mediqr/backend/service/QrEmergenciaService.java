package com.mediqr.backend.service;

import com.mediqr.backend.dto.QrEmergenciaCreateRequest;
import com.mediqr.backend.dto.QrEmergenciaUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.QrEmergencia;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.QrEmergenciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QrEmergenciaService {

    private final QrEmergenciaRepository qrEmergenciaRepository;
    private final PacienteRepository pacienteRepository;

    public QrEmergenciaService(QrEmergenciaRepository qrEmergenciaRepository,
                               PacienteRepository pacienteRepository) {
        this.qrEmergenciaRepository = qrEmergenciaRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public List<QrEmergencia> findAll() {
        return qrEmergenciaRepository.findAll();
    }

    public Optional<QrEmergencia> findById(Long id) {
        return qrEmergenciaRepository.findById(id);
    }

    public List<QrEmergencia> findByPacienteId(Long pacienteId) {
        validatePaciente(pacienteId);
        return qrEmergenciaRepository.findByPacienteId(pacienteId);
    }

    public QrEmergencia getById(Long id) {
        return qrEmergenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR de emergencia no encontrado"));
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
        return qrEmergenciaRepository.save(qr);
    }

    public QrEmergencia update(Long id, QrEmergenciaUpdateRequest request) {
        QrEmergencia qr = getById(id);
        if (request.getMostrarDni() != null) qr.setMostrarDni(request.getMostrarDni());
        if (request.getMostrarTipoSangre() != null) qr.setMostrarTipoSangre(request.getMostrarTipoSangre());
        if (request.getMostrarAlergias() != null) qr.setMostrarAlergias(request.getMostrarAlergias());
        if (request.getMostrarMedicamentos() != null) qr.setMostrarMedicamentos(request.getMostrarMedicamentos());
        if (request.getMostrarEnfermedades() != null) qr.setMostrarEnfermedades(request.getMostrarEnfermedades());
        if (request.getActivo() != null) qr.setActivo(request.getActivo());
        return qrEmergenciaRepository.save(qr);
    }

    public void deleteById(Long id) {
        getById(id);
        qrEmergenciaRepository.deleteById(id);
    }

    private void validatePaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
    }
}
