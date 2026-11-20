package com.mediqr.backend.dto;

import java.time.OffsetDateTime;

public record QrIssueResponse(String token, String publicUrl, OffsetDateTime expiresAt) {
}
