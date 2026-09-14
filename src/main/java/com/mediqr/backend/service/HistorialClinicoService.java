package com.mediqr.backend.service;

import com.mediqr.backend.dto.HistorialClinicoCreateRequest;
import com.mediqr.backend.dto.HistorialClinicoUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.HistorialClinico;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.HistorialClinicoRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialClinicoService {

    private final HistorialClinicoRepository historialClinicoRepository;
    private final PacienteRepository pacienteRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    public HistorialClinicoService(HistorialClinicoRepository historialClinicoRepository,
                                   PacienteRepository pacienteRepository,
                                   RegistroAccesoService registroAccesoService,
                                   CurrentUserService currentUserService) {
        this.historialClinicoRepository = historialClinicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<HistorialClinico> findAll() {
        return historialClinicoRepository.findAll();
    }

    public Optional<HistorialClinico> findById(Long id) {
        return historialClinicoRepository.findById(id);
    }

    public HistorialClinico getById(Long id) {
        HistorialClinico historial = historialClinicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historial clínico no encontrado"));

        registrarAcceso(historial.getPacienteId(), "HISTORIAL_CLINICO", "LECTURA_HISTORIAL");

        return historial;
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
        
        HistorialClinico saved = historialClinicoRepository.save(historialClinico);
        
        registrarAcceso(saved.getPacienteId(), "HISTORIAL_CLINICO", "CREACION_HISTORIAL");

        return saved;
    }

    public HistorialClinico update(Long id, HistorialClinicoUpdateRequest request) {
        HistorialClinico historialClinico = getById(id);
        historialClinico.setTipoSangre(request.getTipoSangre());
        historialClinico.setAlergias(request.getAlergias());
        historialClinico.setEnfermedadesCronicas(request.getEnfermedadesCronicas());
        historialClinico.setCirugias(request.getCirugias());
        historialClinico.setObservaciones(request.getObservaciones());
        
        HistorialClinico saved = historialClinicoRepository.save(historialClinico);
        
        registrarAcceso(saved.getPacienteId(), "HISTORIAL_CLINICO", "ACTUALIZACION_HISTORIAL");

        return saved;
    }

    public void deleteById(Long id) {
        HistorialClinico historial = getById(id);
        Long pacienteId = historial.getPacienteId();
        historialClinicoRepository.deleteById(id);
        
        registrarAcceso(pacienteId, "HISTORIAL_CLINICO", "ELIMINACION_HISTORIAL");
    }

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }
}