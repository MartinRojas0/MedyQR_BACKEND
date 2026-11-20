package com.mediqr.backend.security;

import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.service.UsuarioService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioService usuarioService;

    public UserDetailsServiceImpl(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Usuario usuario = usuarioService.findByLoginIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
        return UserDetailsImpl.build(usuario);
    }
}