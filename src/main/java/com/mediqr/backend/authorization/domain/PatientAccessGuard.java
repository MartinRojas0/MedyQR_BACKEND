package com.mediqr.backend.authorization.domain;

import com.mediqr.backend.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class PatientAccessGuard {

    private final AccessControlService accessControlService;
    private final CurrentUserService currentUserService;

    public PatientAccessGuard(AccessControlService accessControlService,
                              CurrentUserService currentUserService) {
        this.accessControlService = accessControlService;
        this.currentUserService = currentUserService;
    }

    public void requirePatientAccess(Long pacienteId) {
        if (!accessControlService.canAccessPatient(currentUserService.getCurrentUser(), pacienteId)) {
            throw new AccessDeniedException("No tiene acceso al paciente solicitado");
        }
    }
}
