package com.mediqr.backend.service;

import com.mediqr.backend.dto.ConsultaMedicaCreateRequest;
import com.mediqr.backend.dto.ConsultaMedicaUpdateRequest;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.ConsultaMedica;
import com.mediqr.backend.repository.ConsultaMedicaRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsultaMedicaService {

    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final PacienteRepository pacienteRepository;
    private final PersonalSaludRepository personalSaludRepository;

    public ConsultaMedicaService(ConsultaMedicaRepository consultaMedicaRepository,
                                PacienteRepository pacienteRepository,
                                PersonalSaludRepository personalSaludRepository) {
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.personalSaludRepository = personalSaludRepository;
    }

    public List<ConsultaMedica> findAll() {
        return consultaMedicaRepository.findAll();
    }

    public Optional<ConsultaMedica> findById(Long id) {
        return consultaMedicaRepository.findById(id);
    }

    public ConsultaMedica getById(Long id) {
        return consultaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta médica no encontrada"));
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
        return consultaMedicaRepository.save(consultaMedica);
    }

    public ConsultaMedica update(Long id, ConsultaMedicaUpdateRequest request) {
        ConsultaMedica consultaMedica = getById(id);
        consultaMedica.setMotivo(request.getMotivo());
        consultaMedica.setDiagnostico(request.getDiagnostico());
        consultaMedica.setTratamiento(request.getTratamiento());
        consultaMedica.setObservaciones(request.getObservaciones());
        return consultaMedicaRepository.save(consultaMedica);
    }

    public void deleteById(Long id) {
        getById(id);
        consultaMedicaRepository.deleteById(id);
    }

    private void validateReferences(Long pacienteId, Long personalId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
        if (!personalSaludRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal de salud no encontrado");
        }
    }
}
