package com.mediqr.backend.service;

import com.mediqr.backend.model.Cita;
import com.mediqr.backend.repository.CitaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findById(Long id) {
        return citaRepository.findById(id);
    }

    public Cita save(Cita cita) {
        return citaRepository.save(cita);
    }

    public Optional<Cita> update(Long id, Cita cita) {
        return citaRepository.findById(id).map(existingCita -> {
            cita.setId(id);
            return citaRepository.save(cita);
        });
    }

    public void deleteById(Long id) {
        citaRepository.deleteById(id);
    }
}
