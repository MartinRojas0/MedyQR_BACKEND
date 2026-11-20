package com.mediqr.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void accessDeniedIsReturnedAsForbiddenInsteadOfInternalServerError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/pacientes/20");

        ErrorResponse response = handler.handleAccessDenied(new AccessDeniedException("denied"), request);

        assertEquals(403, response.getStatus());
        assertEquals("/api/pacientes/20", response.getPath());
    }
}
