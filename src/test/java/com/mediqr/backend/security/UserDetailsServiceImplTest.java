package com.mediqr.backend.security;

import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import com.mediqr.backend.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserDetailsServiceImpl userDetailsService;
    private Usuario account;

    @BeforeEach
    void setUp() {
        UsuarioService usuarioService = new UsuarioService(usuarioRepository, pacienteRepository, passwordEncoder);
        userDetailsService = new UserDetailsServiceImpl(usuarioService);
        account = new Usuario();
        account.setId(7L);
        account.setEmail("Canonical@Example.com");
        account.setUsername("patient_01");
        account.setPasswordHash("bcrypt-hash");
        account.setRol("PACIENTE");
        account.setActivo(true);

        when(usuarioRepository.findByEmailIgnoreCase("Canonical@Example.com"))
                .thenReturn(Optional.of(account));
        when(usuarioRepository.findByUsernameIgnoreCase("patient_01"))
                .thenReturn(Optional.of(account));
    }

    @Test
    void emailAndUsernameLoadTheSameCanonicalEmailPrincipal() {
        UserDetails byEmail = userDetailsService.loadUserByUsername("Canonical@Example.com");
        UserDetails byUsername = userDetailsService.loadUserByUsername("PATIENT_01");

        assertEquals("Canonical@Example.com", byEmail.getUsername());
        assertEquals(byEmail.getUsername(), byUsername.getUsername());
        assertEquals("bcrypt-hash", byUsername.getPassword());
    }
}
