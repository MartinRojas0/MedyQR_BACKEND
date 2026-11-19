package com.mediqr.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return new CurrentUser(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getRol(),
                    userDetails.getActivo()
            );
        }
        return null;
    }

    public Long getCurrentUserId() {
        CurrentUser user = getCurrentUser();
        return user != null ? user.userId() : null;
    }

    public String getCurrentUserEmail() {
        CurrentUser user = getCurrentUser();
        return user != null ? user.email() : null;
    }

    public String getCurrentUserRol() {
        CurrentUser user = getCurrentUser();
        return user != null ? user.rol() : null;
    }

    public Boolean getCurrentUserActivo() {
        CurrentUser user = getCurrentUser();
        return user != null ? user.activo() : null;
    }

    public boolean hasRole(String role) {
        String currentRole = getCurrentUserRol();
        return currentRole != null && currentRole.equals(role);
    }

    public boolean isPersonalSalud() {
        return hasRole("PERSONAL_SALUD");
    }

    public boolean isPaciente() {
        return hasRole("PACIENTE");
    }

    public record CurrentUser(
            Long userId,
            String email,
            String rol,
            Boolean activo
    ) {}
}