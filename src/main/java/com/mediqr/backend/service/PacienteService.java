package com.mediqr.backend.service;

import com.mediqr.backend.dto.PacienteCreateRequest;
import com.mediqr.backend.dto.PacienteUpdateRequest;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    public PacienteService(PacienteRepository pacienteRepository, UsuarioRepository usuarioRepository,
                           RegistroAccesoService registroAccesoService, CurrentUserService currentUserService) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<Paciente> findAll() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> findById(Long id) {
        return pacienteRepository.findById(id);
    }

    public Paciente getById(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        registrarAcceso(paciente.getId(), "PACIENTE", "LECTURA_PERFIL");

        return paciente;
    }

    public Paciente create(PacienteCreateRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con el ID indicado"));

        if (!"PACIENTE".equals(usuario.getRol())) {
            throw new IllegalArgumentException("El usuario debe tener rol PACIENTE para crear un perfil de paciente");
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

    public Optional<Paciente> findByUsuarioId(Long usuarioId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findByUsuarioId(usuarioId);
        if (pacienteOpt.isPresent()) {
            registrarAcceso(pacienteOpt.get().getId(), "PACIENTE", "LECTURA_PERFIL");
        }
        return pacienteOpt;
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

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }
}