package com.mediqr.backend.service;

import com.mediqr.backend.model.QrEmergencia;
import com.mediqr.backend.repository.QrEmergenciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QrEmergenciaService {

    private final QrEmergenciaRepository qrEmergenciaRepository;

    public QrEmergenciaService(QrEmergenciaRepository qrEmergenciaRepository) {
        this.qrEmergenciaRepository = qrEmergenciaRepository;
    }

    public List<QrEmergencia> findAll() {
        return qrEmergenciaRepository.findAll();
    }

    public Optional<QrEmergencia> findById(Long id) {
        return qrEmergenciaRepository.findById(id);
    }

    public QrEmergencia save(QrEmergencia qrEmergencia) {
        return qrEmergenciaRepository.save(qrEmergencia);
    }

    public Optional<QrEmergencia> update(Long id, QrEmergencia qrEmergencia) {
        return qrEmergenciaRepository.findById(id).map(existingQrEmergencia -> {
            qrEmergencia.setId(id);
            return qrEmergenciaRepository.save(qrEmergencia);
        });
    }

    public void deleteById(Long id) {
        qrEmergenciaRepository.deleteById(id);
    }
}
