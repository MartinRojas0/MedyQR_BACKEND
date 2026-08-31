package com.mediqr.backend.service;

import com.mediqr.backend.model.ConsultaMedica;
import com.mediqr.backend.repository.ConsultaMedicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsultaMedicaService {

    private final ConsultaMedicaRepository consultaMedicaRepository;

    public ConsultaMedicaService(ConsultaMedicaRepository consultaMedicaRepository) {
        this.consultaMedicaRepository = consultaMedicaRepository;
    }

    public List<ConsultaMedica> findAll() {
        return consultaMedicaRepository.findAll();
    }

    public Optional<ConsultaMedica> findById(Long id) {
        return consultaMedicaRepository.findById(id);
    }

    public ConsultaMedica save(ConsultaMedica consultaMedica) {
        return consultaMedicaRepository.save(consultaMedica);
    }

    public Optional<ConsultaMedica> update(Long id, ConsultaMedica consultaMedica) {
        return consultaMedicaRepository.findById(id).map(existingConsultaMedica -> {
            consultaMedica.setId(id);
            return consultaMedicaRepository.save(consultaMedica);
        });
    }

    public void deleteById(Long id) {
        consultaMedicaRepository.deleteById(id);
    }
}
