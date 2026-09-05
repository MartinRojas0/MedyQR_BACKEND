package com.mediqr.backend.service;

import com.mediqr.backend.dto.PacienteCreateRequest;
import com.mediqr.backend.dto.PacienteUpdateRequest;
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

        Paciente paciente = new Paciente();
        paciente.setUsuarioId(request.getUsuarioId());
        paciente.setNombres(request.getNombres());
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
}
