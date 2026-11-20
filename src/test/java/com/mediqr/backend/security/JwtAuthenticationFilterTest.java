package com.mediqr.backend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsJwtForAccountDisabledAfterTokenWasIssued() throws Exception {
        when(jwtUtils.validateJwtToken("signed-token")).thenReturn(true);
        when(jwtUtils.getUsernameFromJwtToken("signed-token")).thenReturn("patient@example.test");
        when(userDetailsService.loadUserByUsername("patient@example.test"))
                .thenReturn(new UserDetailsImpl(7L, "patient@example.test", "hash", "PACIENTE", false));

        MockHttpServletRequest request = requestWithToken();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (req, res) -> chainCalled.set(true));

        assertEquals(401, response.getStatus());
        assertFalse(chainCalled.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void authenticatesEnabledAccount() throws Exception {
        when(jwtUtils.validateJwtToken("signed-token")).thenReturn(true);
        when(jwtUtils.getUsernameFromJwtToken("signed-token")).thenReturn("patient@example.test");
        when(userDetailsService.loadUserByUsername("patient@example.test"))
                .thenReturn(new UserDetailsImpl(7L, "patient@example.test", "hash", "PACIENTE", true));
        AtomicBoolean authenticatedInChain = new AtomicBoolean();

        filter.doFilter(requestWithToken(), new MockHttpServletResponse(), (req, res) ->
                authenticatedInChain.set(
                        SecurityContextHolder.getContext().getAuthentication() != null
                                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()));

        assertTrue(authenticatedInChain.get());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private MockHttpServletRequest requestWithToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer signed-token");
        return request;
    }
}
