package com.mediqr.backend.service;

import com.mediqr.backend.dto.UsuarioCreateRequest;
import com.mediqr.backend.dto.UsuarioResponse;
import com.mediqr.backend.dto.UsuarioUpdateRequest;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    private UsuarioService usuarioService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, pacienteRepository, passwordEncoder);
    }

    @Test
    void createPublicRegistrationAlwaysAssignsPacienteRole() {
        UsuarioCreateRequest request = new UsuarioCreateRequest();
        request.setEmail("test@test.com");
        request.setPassword("secret123");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse response = usuarioService.create(request);

        assertEquals("PACIENTE", response.getRol());
        verify(usuarioRepository).existsByEmailIgnoreCase("test@test.com");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(pacienteRepository).save(org.mockito.ArgumentMatchers.argThat(
                paciente -> Long.valueOf(1L).equals(paciente.getUsuarioId()) &&
                        "".equals(paciente.getNombres()) && "".equals(paciente.getApellidos())));
    }

    @Test
    void createRejectsDuplicateEmailWithoutCreatingAccountOrPatient() {
        UsuarioCreateRequest request = new UsuarioCreateRequest();
        request.setEmail("test@test.com");
        request.setPassword("secret123");
        when(usuarioRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        assertThrows(com.mediqr.backend.exception.BusinessRuleException.class,
                () -> usuarioService.create(request));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(pacienteRepository);
    }

    @Test
    void updateRejectsEmailUsedByAnotherUser() {
        Usuario existing = new Usuario();
        existing.setId(1L);
        existing.setEmail("current@example.com");
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setEmail("other@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("other@example.com", 1L)).thenReturn(true);

        assertThrows(com.mediqr.backend.exception.BusinessRuleException.class,
                () -> usuarioService.update(1L, request));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void updateDoesNotAllowChangingRoleAndPreservesAccountStateUpdates() {
        Usuario existing = new Usuario();
        existing.setId(1L);
        existing.setEmail("patient@example.com");
        existing.setRol("PACIENTE");
        existing.setActivo(true);
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setRol("PERSONAL_SALUD");
        request.setActivo(false);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(usuarioRepository.save(existing)).thenReturn(existing);

        UsuarioResponse response = usuarioService.update(1L, request).orElseThrow();

        assertEquals("PACIENTE", response.getRol());
        assertFalse(response.getActivo());
    }

    @Test
    void createIgnoresClientProvidedRole() {
        UsuarioCreateRequest request = new UsuarioCreateRequest();
        request.setEmail("test@test.com");
        request.setPassword("secret123");
        // Cliente intenta enviar rol PERSONAL_SALUD - debería ser ignorado
        // (el DTO ya no tiene campo rol, pero verificamos que el servicio lo fuerza a PACIENTE)

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse response = usuarioService.create(request);

        assertEquals("PACIENTE", response.getRol());
        verify(pacienteRepository).save(any(Paciente.class));
    }
}