package com.mediqr.backend.service;

import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public List<Paciente> findAll() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> findById(Long id) {
        return pacienteRepository.findById(id);
    }

    public Paciente save(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public Optional<Paciente> update(Long id, Paciente paciente) {
        return pacienteRepository.findById(id).map(existingPaciente -> {
            paciente.setId(id);
            return pacienteRepository.save(paciente);
        });
    }

    public void deleteById(Long id) {
        pacienteRepository.deleteById(id);
    }
}
