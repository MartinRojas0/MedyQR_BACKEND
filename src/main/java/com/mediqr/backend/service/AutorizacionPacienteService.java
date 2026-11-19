package com.mediqr.backend.service;

import com.mediqr.backend.model.AutorizacionPaciente;
import com.mediqr.backend.repository.AutorizacionPacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AutorizacionPacienteService {

    private final AutorizacionPacienteRepository autorizacionPacienteRepository;

    public AutorizacionPacienteService(AutorizacionPacienteRepository autorizacionPacienteRepository) {
        this.autorizacionPacienteRepository = autorizacionPacienteRepository;
    }

    public List<AutorizacionPaciente> findAll() {
        return autorizacionPacienteRepository.findAll();
    }

    public Optional<AutorizacionPaciente> findById(Long id) {
        return autorizacionPacienteRepository.findById(id);
    }

    public AutorizacionPaciente save(AutorizacionPaciente autorizacionPaciente) {
        return autorizacionPacienteRepository.save(autorizacionPaciente);
    }

    public Optional<AutorizacionPaciente> update(Long id, AutorizacionPaciente autorizacionPaciente) {
        return autorizacionPacienteRepository.findById(id).map(existingAutorizacionPaciente -> {
            autorizacionPaciente.setId(id);
            return autorizacionPacienteRepository.save(autorizacionPaciente);
        });
    }

    public void deleteById(Long id) {
        autorizacionPacienteRepository.deleteById(id);
    }
}
