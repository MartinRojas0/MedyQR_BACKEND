package com.mediqr.backend.authorization.domain;

import com.mediqr.backend.security.CurrentUserService;

public interface AccessControlService {

    boolean canAccessPatient(CurrentUserService.CurrentUser currentUser, Long pacienteId);
}