package com.mediqr.backend.service;

import com.mediqr.backend.dto.UsuarioCreateRequest;
import com.mediqr.backend.dto.UsuarioResponse;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Usuario;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService usuarioService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
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
        verify(usuarioRepository).save(any(Usuario.class));
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
    }
}