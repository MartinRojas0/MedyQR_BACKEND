package com.mediqr.backend.service;

import com.mediqr.backend.dto.PersonalSaludCreateRequest;
import com.mediqr.backend.dto.PersonalSaludUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonalSaludService {

    private final PersonalSaludRepository personalSaludRepository;
    private final UsuarioRepository usuarioRepository;

    public PersonalSaludService(PersonalSaludRepository personalSaludRepository,
                                UsuarioRepository usuarioRepository) {
        this.personalSaludRepository = personalSaludRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<PersonalSalud> findAll() {
        return personalSaludRepository.findAll();
    }

    public Optional<PersonalSalud> findById(Long id) {
        return personalSaludRepository.findById(id);
    }

    public PersonalSalud getById(Long id) {
        return personalSaludRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal de salud no encontrado"));
    }

    public PersonalSalud create(PersonalSaludCreateRequest request) {
        if (!usuarioRepository.existsById(request.getUsuarioId())) {
            throw new ResourceNotFoundException("No existe un usuario con el ID indicado");
        }
        if (personalSaludRepository.existsByUsuarioId(request.getUsuarioId())) {
            throw new BusinessRuleException("El usuario ya tiene un perfil de personal de salud");
        }
        String documento = normalize(request.getDocumentoIdentidad());
        String registro = request.getRegistroProfesional().trim();
        if (documento != null && personalSaludRepository.existsByDocumentoIdentidad(documento)) {
            throw new BusinessRuleException("Ya existe personal de salud con ese documento de identidad");
        }
        if (personalSaludRepository.existsByRegistroProfesional(registro)) {
            throw new BusinessRuleException("Ya existe personal de salud con ese registro profesional");
        }

        PersonalSalud personal = new PersonalSalud();
        personal.setUsuarioId(request.getUsuarioId());
        apply(personal, request.getNombres(), request.getApellidos(), documento,
                request.getTelefono(), request.getEspecialidad(), registro);
        return personalSaludRepository.save(personal);
    }

    public PersonalSalud update(Long id, PersonalSaludUpdateRequest request) {
        PersonalSalud personal = getById(id);
        String documento = normalize(request.getDocumentoIdentidad());
        String registro = request.getRegistroProfesional().trim();
        if (documento != null && personalSaludRepository.existsByDocumentoIdentidadAndIdNot(documento, id)) {
            throw new BusinessRuleException("Ya existe personal de salud con ese documento de identidad");
        }
        if (personalSaludRepository.existsByRegistroProfesionalAndIdNot(registro, id)) {
            throw new BusinessRuleException("Ya existe personal de salud con ese registro profesional");
        }

        apply(personal, request.getNombres(), request.getApellidos(), documento,
                request.getTelefono(), request.getEspecialidad(), registro);
        if (request.getActivo() != null) {
            personal.setActivo(request.getActivo());
        }
        return personalSaludRepository.save(personal);
    }

    public void deleteById(Long id) {
        getById(id);
        personalSaludRepository.deleteById(id);
    }

    private void apply(PersonalSalud personal, String nombres, String apellidos, String documento,
                       String telefono, String especialidad, String registro) {
        personal.setNombres(nombres.trim());
        personal.setApellidos(apellidos.trim());
        personal.setDocumentoIdentidad(documento);
        personal.setTelefono(telefono);
        personal.setEspecialidad(especialidad);
        personal.setRegistroProfesional(registro);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}