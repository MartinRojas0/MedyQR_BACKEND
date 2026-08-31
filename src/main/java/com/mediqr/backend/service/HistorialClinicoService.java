package com.mediqr.backend.service;

import com.mediqr.backend.model.HistorialClinico;
import com.mediqr.backend.repository.HistorialClinicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialClinicoService {

    private final HistorialClinicoRepository historialClinicoRepository;

    public HistorialClinicoService(HistorialClinicoRepository historialClinicoRepository) {
        this.historialClinicoRepository = historialClinicoRepository;
    }

    public List<HistorialClinico> findAll() {
        return historialClinicoRepository.findAll();
    }

    public Optional<HistorialClinico> findById(Long id) {
        return historialClinicoRepository.findById(id);
    }

    public HistorialClinico save(HistorialClinico historialClinico) {
        return historialClinicoRepository.save(historialClinico);
    }

    public Optional<HistorialClinico> update(Long id, HistorialClinico historialClinico) {
        return historialClinicoRepository.findById(id).map(existingHistorialClinico -> {
            historialClinico.setId(id);
            return historialClinicoRepository.save(historialClinico);
        });
    }

    public void deleteById(Long id) {
        historialClinicoRepository.deleteById(id);
    }
}
