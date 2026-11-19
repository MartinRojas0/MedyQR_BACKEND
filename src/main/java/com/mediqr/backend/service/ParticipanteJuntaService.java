package com.mediqr.backend.service;

import com.mediqr.backend.model.ParticipanteJunta;
import com.mediqr.backend.repository.ParticipanteJuntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipanteJuntaService {

    private final ParticipanteJuntaRepository participanteJuntaRepository;

    public ParticipanteJuntaService(ParticipanteJuntaRepository participanteJuntaRepository) {
        this.participanteJuntaRepository = participanteJuntaRepository;
    }

    public List<ParticipanteJunta> findAll() {
        return participanteJuntaRepository.findAll();
    }

    public Optional<ParticipanteJunta> findById(Long id) {
        return participanteJuntaRepository.findById(id);
    }

    public ParticipanteJunta save(ParticipanteJunta participanteJunta) {
        return participanteJuntaRepository.save(participanteJunta);
    }

    public Optional<ParticipanteJunta> update(Long id, ParticipanteJunta participanteJunta) {
        return participanteJuntaRepository.findById(id).map(existingParticipanteJunta -> {
            participanteJunta.setId(id);
            return participanteJuntaRepository.save(participanteJunta);
        });
    }

    public void deleteById(Long id) {
        participanteJuntaRepository.deleteById(id);
    }
}
