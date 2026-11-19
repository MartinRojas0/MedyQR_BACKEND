package com.mediqr.backend.service;

import com.mediqr.backend.dto.HistorialClinicoCreateRequest;
import com.mediqr.backend.dto.HistorialClinicoUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.HistorialClinico;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.HistorialClinicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialClinicoService {

    private final HistorialClinicoRepository historialClinicoRepository;
    private final PacienteRepository pacienteRepository;

    public HistorialClinicoService(HistorialClinicoRepository historialClinicoRepository,
                                   PacienteRepository pacienteRepository) {
        this.historialClinicoRepository = historialClinicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public List<HistorialClinico> findAll() {
        return historialClinicoRepository.findAll();
    }

    public Optional<HistorialClinico> findById(Long id) {
        return historialClinicoRepository.findById(id);
    }

    public HistorialClinico getById(Long id) {
        return historialClinicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historial clínico no encontrado"));
    }

    public HistorialClinico create(HistorialClinicoCreateRequest request) {
        if (!pacienteRepository.existsById(request.getPacienteId())) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
        if (historialClinicoRepository.existsByPacienteId(request.getPacienteId())) {
            throw new BusinessRuleException("El paciente ya tiene un historial clínico");
        }

        HistorialClinico historialClinico = new HistorialClinico();
        historialClinico.setPacienteId(request.getPacienteId());
        historialClinico.setTipoSangre(request.getTipoSangre());
        historialClinico.setAlergias(request.getAlergias());
        historialClinico.setEnfermedadesCronicas(request.getEnfermedadesCronicas());
        historialClinico.setCirugias(request.getCirugias());
        historialClinico.setObservaciones(request.getObservaciones());
        return historialClinicoRepository.save(historialClinico);
    }

    public HistorialClinico update(Long id, HistorialClinicoUpdateRequest request) {
        HistorialClinico historialClinico = getById(id);
        historialClinico.setTipoSangre(request.getTipoSangre());
        historialClinico.setAlergias(request.getAlergias());
        historialClinico.setEnfermedadesCronicas(request.getEnfermedadesCronicas());
        historialClinico.setCirugias(request.getCirugias());
        historialClinico.setObservaciones(request.getObservaciones());
        return historialClinicoRepository.save(historialClinico);
    }

    public void deleteById(Long id) {
        getById(id);
        historialClinicoRepository.deleteById(id);
    }
}
