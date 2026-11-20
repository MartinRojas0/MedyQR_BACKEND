package com.mediqr.backend.authorization.domain;

import com.mediqr.backend.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientAccessGuardTest {

    @Test
    void deniesProfessionalWithoutActivePatientAuthorization() {
        AccessControlService accessControlService = mock(AccessControlService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        var professional = new CurrentUserService.CurrentUser(17L, "professional@example.test",
                "PERSONAL_SALUD", true);
        when(currentUserService.getCurrentUser()).thenReturn(professional);
        when(accessControlService.canAccessPatient(professional, 42L)).thenReturn(false);

        PatientAccessGuard guard = new PatientAccessGuard(accessControlService, currentUserService);

        assertThrows(AccessDeniedException.class, () -> guard.requirePatientAccess(42L));
    }
}
