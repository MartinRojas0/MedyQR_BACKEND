package com.mediqr.backend.service;

import com.mediqr.backend.dto.ConsultaMedicaCreateRequest;
import com.mediqr.backend.dto.ConsultaMedicaUpdateRequest;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.ConsultaMedica;
import com.mediqr.backend.repository.ConsultaMedicaRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsultaMedicaService {

    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final PacienteRepository pacienteRepository;
    private final PersonalSaludRepository personalSaludRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    public ConsultaMedicaService(ConsultaMedicaRepository consultaMedicaRepository,
                                 PacienteRepository pacienteRepository,
                                 PersonalSaludRepository personalSaludRepository,
                                 RegistroAccesoService registroAccesoService,
                                 CurrentUserService currentUserService) {
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.personalSaludRepository = personalSaludRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<ConsultaMedica> findAll() {
        return consultaMedicaRepository.findAll();
    }

    public Optional<ConsultaMedica> findById(Long id) {
        return consultaMedicaRepository.findById(id);
    }

    public ConsultaMedica getById(Long id) {
        ConsultaMedica consulta = consultaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta médica no encontrada"));

        registrarAcceso(consulta.getPacienteId(), "CONSULTA_MEDICA", "LECTURA_CONSULTA");

        return consulta;
    }

    public ConsultaMedica create(ConsultaMedicaCreateRequest request) {
        validateReferences(request.getPacienteId(), request.getPersonalId());

        ConsultaMedica consultaMedica = new ConsultaMedica();
        consultaMedica.setPacienteId(request.getPacienteId());
        consultaMedica.setPersonalId(request.getPersonalId());
        consultaMedica.setFechaConsulta(request.getFechaConsulta());
        consultaMedica.setMotivo(request.getMotivo());
        consultaMedica.setDiagnostico(request.getDiagnostico());
        consultaMedica.setTratamiento(request.getTratamiento());
        consultaMedica.setObservaciones(request.getObservaciones());
        
        ConsultaMedica saved = consultaMedicaRepository.save(consultaMedica);
        
        registrarAcceso(saved.getPacienteId(), "CONSULTA_MEDICA", "CREACION_CONSULTA");

        return saved;
    }

    public ConsultaMedica update(Long id, ConsultaMedicaUpdateRequest request) {
        ConsultaMedica consultaMedica = getById(id);
        consultaMedica.setMotivo(request.getMotivo());
        consultaMedica.setDiagnostico(request.getDiagnostico());
        consultaMedica.setTratamiento(request.getTratamiento());
        consultaMedica.setObservaciones(request.getObservaciones());
        
        ConsultaMedica saved = consultaMedicaRepository.save(consultaMedica);
        
        registrarAcceso(saved.getPacienteId(), "CONSULTA_MEDICA", "ACTUALIZACION_CONSULTA");

        return saved;
    }

    public void deleteById(Long id) {
        ConsultaMedica consulta = getById(id);
        Long pacienteId = consulta.getPacienteId();
        consultaMedicaRepository.deleteById(id);
        
        registrarAcceso(pacienteId, "CONSULTA_MEDICA", "ELIMINACION_CONSULTA");
    }

    private void validateReferences(Long pacienteId, Long personalId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
        if (!personalSaludRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal de salud no encontrado");
        }
    }

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }
}