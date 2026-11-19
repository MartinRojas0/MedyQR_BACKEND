package com.mediqr.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilsTest {

    @Test
    void generatedTokenCanBeValidatedAndRead() {
        JwtUtils jwtUtils = new JwtUtils();
        String secret = Base64.getEncoder().encodeToString(
                "test-secret-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60_000L);

        UserDetailsImpl user = new UserDetailsImpl(42L, "patient@example.test", "hash", "PACIENTE", true);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        String token = jwtUtils.generateJwtToken(authentication);

        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("patient@example.test", jwtUtils.getUsernameFromJwtToken(token));
        assertEquals(42L, jwtUtils.getUserIdFromJwtToken(token));
        assertEquals("PACIENTE", jwtUtils.getRolFromJwtToken(token));
    }
}
