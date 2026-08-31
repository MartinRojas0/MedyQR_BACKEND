package com.mediqr.backend.service;

import com.mediqr.backend.model.JuntaMedica;
import com.mediqr.backend.repository.JuntaMedicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JuntaMedicaService {

    private final JuntaMedicaRepository juntaMedicaRepository;

    public JuntaMedicaService(JuntaMedicaRepository juntaMedicaRepository) {
        this.juntaMedicaRepository = juntaMedicaRepository;
    }

    public List<JuntaMedica> findAll() {
        return juntaMedicaRepository.findAll();
    }

    public Optional<JuntaMedica> findById(Long id) {
        return juntaMedicaRepository.findById(id);
    }

    public JuntaMedica save(JuntaMedica juntaMedica) {
        return juntaMedicaRepository.save(juntaMedica);
    }

    public Optional<JuntaMedica> update(Long id, JuntaMedica juntaMedica) {
        return juntaMedicaRepository.findById(id).map(existingJuntaMedica -> {
            juntaMedica.setId(id);
            return juntaMedicaRepository.save(juntaMedica);
        });
    }

    public void deleteById(Long id) {
        juntaMedicaRepository.deleteById(id);
    }
}
