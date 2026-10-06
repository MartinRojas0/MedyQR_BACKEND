package com.mediqr.backend.service;

import com.mediqr.backend.dto.PacienteCreateRequest;
import com.mediqr.backend.dto.PacienteUpdateRequest;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public PacienteService(PacienteRepository pacienteRepository, UsuarioRepository usuarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Paciente> findAll() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> findById(Long id) {
        return pacienteRepository.findById(id);
    }

    public Paciente getById(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
    }

    public Paciente create(PacienteCreateRequest request) {
        if (!usuarioRepository.existsById(request.getUsuarioId())) {
            throw new ResourceNotFoundException("No existe un usuario con el ID indicado");
        }
        if (pacienteRepository.existsByUsuarioId(request.getUsuarioId())) {
        throw new BusinessRuleException("El usuario ya tiene un paciente asociado");
    }
    String documento = normalize(request.getDocumentoIdentidad());
    if (documento != null && pacienteRepository.existsByDocumentoIdentidad(documento)) {
        throw new BusinessRuleException("Ya existe un paciente con ese documento de identidad");
    }

        Paciente paciente = new Paciente();
        paciente.setUsuarioId(request.getUsuarioId());
        paciente.setNombres(request.getNombres().trim());      // antes: sin trim
        paciente.setDocumentoIdentidad(documento);    //nuevo         
        paciente.setApellidos(request.getApellidos());
        paciente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setTelefono(request.getTelefono());
        paciente.setDireccion(request.getDireccion());
        paciente.setSexo(request.getSexo());
        return pacienteRepository.save(paciente);
    }

    public Paciente update(Long id, PacienteUpdateRequest request) {
        Paciente paciente = getById(id);
    String documento = normalize(request.getDocumentoIdentidad());
    if (documento != null && pacienteRepository.existsByDocumentoIdentidadAndIdNot(documento, id)) {
        throw new BusinessRuleException("Ya existe un paciente con ese documento de identidad");
    }//validacion de documento de identidad
        
        paciente.setNombres(request.getNombres());
        paciente.setApellidos(request.getApellidos());
        paciente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setTelefono(request.getTelefono());
        paciente.setDireccion(request.getDireccion());
        paciente.setSexo(request.getSexo());
        return pacienteRepository.save(paciente);
    }

    public void deleteById(Long id) {
        getById(id);
        pacienteRepository.deleteById(id);
    }
    private String normalize(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
}
}
