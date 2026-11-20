package com.mediqr.backend.dto;

import java.time.OffsetDateTime;

public record QrStatusResponse(boolean activo, OffsetDateTime expiresAt) {
}
