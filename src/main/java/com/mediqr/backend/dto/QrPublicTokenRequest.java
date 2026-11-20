package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QrPublicTokenRequest(@NotBlank @Size(max = 64) String token) {
}
