package com.mediqr.backend.service;

import com.mediqr.backend.dto.MedicamentoCreateRequest;
import com.mediqr.backend.dto.MedicamentoUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.repository.MedicamentoRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    public MedicamentoService(MedicamentoRepository medicamentoRepository,
                               PacienteRepository pacienteRepository,
                               RegistroAccesoService registroAccesoService,
                               CurrentUserService currentUserService) {
        this.medicamentoRepository = medicamentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<Medicamento> findAll() {
        return medicamentoRepository.findAll();
    }

    public Optional<Medicamento> findById(Long id) {
        return medicamentoRepository.findById(id);
    }

    public List<Medicamento> findByPacienteId(Long pacienteId) {
        validatePatient(pacienteId);
        
        List<Medicamento> medicamentos = medicamentoRepository.findByPacienteId(pacienteId);
        
        if (!medicamentos.isEmpty()) {
            registrarAcceso(pacienteId, "MEDICAMENTO", "LECTURA_LISTA_MEDICAMENTOS");
        }

        return medicamentos;
    }

    public Medicamento getById(Long id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado"));

        registrarAcceso(medicamento.getPacienteId(), "MEDICAMENTO", "LECTURA_MEDICAMENTO");

        return medicamento;
    }

    public Medicamento create(MedicamentoCreateRequest request) {
        validatePatient(request.getPacienteId());
        validateDates(request.getFechaInicio(), request.getFechaFin());

        Medicamento medicamento = new Medicamento();
        medicamento.setPacienteId(request.getPacienteId());
        medicamento.setNombre(request.getNombre());
        medicamento.setDosis(request.getDosis());
        medicamento.setFrecuencia(request.getFrecuencia());
        medicamento.setFechaInicio(request.getFechaInicio());
        medicamento.setFechaFin(request.getFechaFin());
        medicamento.setInstrucciones(request.getInstrucciones());
        
        Medicamento saved = medicamentoRepository.save(medicamento);
        
        registrarAcceso(saved.getPacienteId(), "MEDICAMENTO", "CREACION_MEDICAMENTO");

        return saved;
    }

    public Medicamento update(Long id, MedicamentoUpdateRequest request) {
        Medicamento medicamento = getById(id);
        validateDates(request.getFechaInicio(), request.getFechaFin());
        medicamento.setNombre(request.getNombre());
        medicamento.setDosis(request.getDosis());
        medicamento.setFrecuencia(request.getFrecuencia());
        medicamento.setFechaInicio(request.getFechaInicio());
        medicamento.setFechaFin(request.getFechaFin());
        medicamento.setInstrucciones(request.getInstrucciones());
        if (request.getActivo() != null) {
            medicamento.setActivo(request.getActivo());
        }
        
        Medicamento saved = medicamentoRepository.save(medicamento);
        
        registrarAcceso(saved.getPacienteId(), "MEDICAMENTO", "ACTUALIZACION_MEDICAMENTO");

        return saved;
    }

    public void deleteById(Long id) {
        Medicamento medicamento = getById(id);
        Long pacienteId = medicamento.getPacienteId();
        medicamentoRepository.deleteById(id);
        
        registrarAcceso(pacienteId, "MEDICAMENTO", "ELIMINACION_MEDICAMENTO");
    }

    private void validatePatient(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
    }

    private void validateDates(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new BusinessRuleException("La fecha fin no puede ser anterior a la fecha inicio");
        }
    }

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }
}