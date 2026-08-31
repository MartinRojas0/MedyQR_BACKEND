package com.mediqr.backend.service;

import com.mediqr.backend.model.RegistroAcceso;
import com.mediqr.backend.repository.RegistroAccesoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegistroAccesoService {

    private final RegistroAccesoRepository registroAccesoRepository;

    public RegistroAccesoService(RegistroAccesoRepository registroAccesoRepository) {
        this.registroAccesoRepository = registroAccesoRepository;
    }

    public List<RegistroAcceso> findAll() {
        return registroAccesoRepository.findAll();
    }

    public Optional<RegistroAcceso> findById(Long id) {
        return registroAccesoRepository.findById(id);
    }

    public RegistroAcceso save(RegistroAcceso registroAcceso) {
        return registroAccesoRepository.save(registroAcceso);
    }

    public Optional<RegistroAcceso> update(Long id, RegistroAcceso registroAcceso) {
        return registroAccesoRepository.findById(id).map(existingRegistroAcceso -> {
            registroAcceso.setId(id);
            return registroAccesoRepository.save(registroAcceso);
        });
    }

    public void deleteById(Long id) {
        registroAccesoRepository.deleteById(id);
    }
}
