package com.mediqr.backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioCreateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void normalizesUsernameBeforeValidatingItsAllowedFormat() {
        UsuarioCreateRequest request = validRequest();
        request.setUsername("  Patient-01._  ");

        assertEquals("patient-01._", request.getUsername());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsUsernameOutsideTheConservativeFormat() {
        UsuarioCreateRequest request = validRequest();
        request.setUsername("ab");

        assertFalse(validator.validate(request).isEmpty());
    }

    private UsuarioCreateRequest validRequest() {
        UsuarioCreateRequest request = new UsuarioCreateRequest();
        request.setEmail("patient@example.com");
        request.setPassword("secret123");
        return request;
    }
}
