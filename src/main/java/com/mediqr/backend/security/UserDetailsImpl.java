package com.mediqr.backend.security;

import com.mediqr.backend.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String rol;
    private final Boolean activo;

    public UserDetailsImpl(Long id, String email, String passwordHash, String rol, Boolean activo) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public static UserDetailsImpl build(Usuario usuario) {
        return new UserDetailsImpl(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getRol(),
                usuario.getActivo()
        );
    }

    public Long getId() {
        return id;
    }

    public String getRol() {
        return rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return activo != null && activo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo != null && activo;
    }
}