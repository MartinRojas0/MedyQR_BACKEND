package com.mediqr.backend.service;

import com.mediqr.backend.dto.PacienteCreateRequest;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RegistroAccesoService registroAccesoService;

    @Mock
    private CurrentUserService currentUserService;

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        pacienteService = new PacienteService(pacienteRepository, usuarioRepository, registroAccesoService, currentUserService);
    }

    @Test
    void createWithPacienteUserSucceeds() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRol("PACIENTE");

        PacienteCreateRequest request = new PacienteCreateRequest();
        request.setUsuarioId(1L);
        request.setNombres("Juan");
        request.setApellidos("Perez");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        Paciente result = pacienteService.create(request);

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getUsuarioId());
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    void createWithPersonalSaludUserThrowsException() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setRol("PERSONAL_SALUD");

        PacienteCreateRequest request = new PacienteCreateRequest();
        request.setUsuarioId(2L);
        request.setNombres("Juan");
        request.setApellidos("Perez");

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pacienteService.create(request));

        assertEquals("El usuario debe tener rol PACIENTE para crear un perfil de paciente", ex.getMessage());
    }

    @Test
    void createWithNonExistentUserThrowsException() {
        PacienteCreateRequest request = new PacienteCreateRequest();
        request.setUsuarioId(999L);
        request.setNombres("Juan");
        request.setApellidos("Perez");

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pacienteService.create(request));
    }

    @Test
    void createWithUnknownRoleThrowsException() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setRol("UNKNOWN");

        PacienteCreateRequest request = new PacienteCreateRequest();
        request.setUsuarioId(3L);
        request.setNombres("Juan");
        request.setApellidos("Perez");

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () -> pacienteService.create(request));
    }
}