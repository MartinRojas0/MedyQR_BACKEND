package com.mediqr.backend.service;

import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.PersonalSaludRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonalSaludService {

    private final PersonalSaludRepository personalSaludRepository;

    public PersonalSaludService(PersonalSaludRepository personalSaludRepository) {
        this.personalSaludRepository = personalSaludRepository;
    }

    public List<PersonalSalud> findAll() {
        return personalSaludRepository.findAll();
    }

    public Optional<PersonalSalud> findById(Long id) {
        return personalSaludRepository.findById(id);
    }

    public PersonalSalud save(PersonalSalud personalSalud) {
        return personalSaludRepository.save(personalSalud);
    }

    public Optional<PersonalSalud> update(Long id, PersonalSalud personalSalud) {
        return personalSaludRepository.findById(id).map(existingPersonalSalud -> {
            personalSalud.setId(id);
            return personalSaludRepository.save(personalSalud);
        });
    }

    public void deleteById(Long id) {
        personalSaludRepository.deleteById(id);
    }

    public Optional<PersonalSalud> findByUsuarioId(Long usuarioId) {
        return personalSaludRepository.findByUsuarioId(usuarioId);
    }
}
