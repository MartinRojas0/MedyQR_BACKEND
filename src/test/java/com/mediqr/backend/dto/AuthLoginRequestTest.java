package com.mediqr.backend.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthLoginRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void acceptsLegacyEmailAndUsernameLoginPayloads() throws Exception {
        AuthLoginRequest legacy = objectMapper.readValue(
                "{\"email\":\"patient@example.com\",\"password\":\"secret123\"}",
                AuthLoginRequest.class);
        AuthLoginRequest username = objectMapper.readValue(
                "{\"username\":\"patient_01\",\"password\":\"secret123\"}",
                AuthLoginRequest.class);

        assertEquals("patient@example.com", legacy.getIdentifier());
        assertEquals("patient_01", username.getIdentifier());
        assertEquals("secret123", legacy.getPassword());
    }
}
