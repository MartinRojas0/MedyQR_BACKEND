package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmergencyContactUpdateRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 80) String parentesco,
        @NotBlank @Size(max = 30)
        @Pattern(regexp = "^[0-9+(). -]{7,30}$") String telefono
) {
}
