package com.mediqr.backend.controller;

import com.mediqr.backend.dto.AuthLoginRequest;
import com.mediqr.backend.dto.AuthMeResponse;
import com.mediqr.backend.dto.AuthResponse;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.security.JwtUtils;
import com.mediqr.backend.security.UserDetailsImpl;
import com.mediqr.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioService usuarioService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UsuarioService usuarioService, CurrentUserService currentUserService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.usuarioService = usuarioService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        AuthResponse response = new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getRol());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> getCurrentUser() {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        var usuarioOpt = usuarioService.findByEmail(currentUser.email());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var usuario = usuarioOpt.get();
        AuthMeResponse response = new AuthMeResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getCreatedAt(),
                usuario.getUpdatedAt()
        );
        return ResponseEntity.ok(response);
    }
}