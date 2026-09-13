package com.mediqr.backend.service;

import com.mediqr.backend.dto.UsuarioCreateRequest;
import com.mediqr.backend.dto.UsuarioResponse;
import com.mediqr.backend.dto.UsuarioUpdateRequest;
import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<UsuarioResponse> findById(Long id) {
        return usuarioRepository.findById(id).map(this::toResponse);
    }

    public UsuarioResponse create(UsuarioCreateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol() != null ? request.getRol() : "PACIENTE");
        usuario.setActivo(true);
        Usuario saved = usuarioRepository.save(usuario);
        return toResponse(saved);
    }

    public Optional<UsuarioResponse> update(Long id, UsuarioUpdateRequest request) {
        return usuarioRepository.findById(id).map(existing -> {
            if (request.getEmail() != null) {
                existing.setEmail(request.getEmail());
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                existing.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            if (request.getRol() != null) {
                existing.setRol(request.getRol());
            }
            if (request.getActivo() != null) {
                existing.setActivo(request.getActivo());
            }
            return toResponse(usuarioRepository.save(existing));
        });
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getCreatedAt(),
                usuario.getUpdatedAt()
        );
    }
}
