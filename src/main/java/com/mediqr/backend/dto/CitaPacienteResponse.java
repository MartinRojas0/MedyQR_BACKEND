package com.mediqr.backend.dto;

import java.time.OffsetDateTime;

public record CitaPacienteResponse(
        Long id,
        Long personalId,
        String profesional,
        String especialidad,
        OffsetDateTime fechaHora,
        String motivo,
        String estado,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
