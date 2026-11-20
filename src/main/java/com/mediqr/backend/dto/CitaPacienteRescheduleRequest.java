package com.mediqr.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class CitaPacienteRescheduleRequest {

    @NotNull
    @Future
    private OffsetDateTime fechaHora;

    public OffsetDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(OffsetDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
