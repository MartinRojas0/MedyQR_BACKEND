package com.mediqr.backend.service;

import com.mediqr.backend.dto.MedicamentoCreateRequest;
import com.mediqr.backend.dto.MedicamentoUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.repository.MedicamentoRepository;
import com.mediqr.backend.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final PacienteRepository pacienteRepository;

    public MedicamentoService(MedicamentoRepository medicamentoRepository,
                              PacienteRepository pacienteRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public List<Medicamento> findAll() {
        return medicamentoRepository.findAll();
    }

    public Optional<Medicamento> findById(Long id) {
        return medicamentoRepository.findById(id);
    }

    public List<Medicamento> findByPacienteId(Long pacienteId) {
        validatePatient(pacienteId);
        return medicamentoRepository.findByPacienteId(pacienteId);
    }

    public Medicamento getById(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado"));
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
        return medicamentoRepository.save(medicamento);
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
        return medicamentoRepository.save(medicamento);
    }

    public void deleteById(Long id) {
        getById(id);
        medicamentoRepository.deleteById(id);
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
}
