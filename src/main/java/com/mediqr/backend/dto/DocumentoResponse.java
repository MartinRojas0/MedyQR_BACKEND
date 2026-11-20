package com.mediqr.backend.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DocumentoResponse(
        Long id,
        String nombreArchivo,
        String tipo,
        LocalDate fechaDocumento,
        Long citaId,
        String contentType,
        long sizeBytes,
        OffsetDateTime createdAt
) {
}
