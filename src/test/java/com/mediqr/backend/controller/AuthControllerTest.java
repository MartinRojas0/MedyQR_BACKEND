package com.mediqr.backend.controller;

import com.mediqr.backend.dto.AuthLoginRequest;
import com.mediqr.backend.exception.ErrorResponse;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.security.JwtUtils;
import com.mediqr.backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void returnsSameGenericFailureForUnknownAccountAndBadPassword() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        AuthController controller = new AuthController(
                authenticationManager, mock(JwtUtils.class), mock(UsuarioService.class),
                mock(CurrentUserService.class));
        AuthLoginRequest request = new AuthLoginRequest();
        request.setIdentifier("unknown_user");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new UsernameNotFoundException("not found"),
                        new BadCredentialsException("bad password"));

        var unknown = controller.login(request);
        var badPassword = controller.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, unknown.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, badPassword.getStatusCode());
        assertEquals(((ErrorResponse) unknown.getBody()).getMessage(),
                ((ErrorResponse) badPassword.getBody()).getMessage());
    }
}
