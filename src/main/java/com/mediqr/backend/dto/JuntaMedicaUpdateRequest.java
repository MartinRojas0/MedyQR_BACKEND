package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public class JuntaMedicaUpdateRequest {

    private String motivo;

    private OffsetDateTime fechaInicio;

    private OffsetDateTime fechaExpiracion;

    private String estado;

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public OffsetDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(OffsetDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public OffsetDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(OffsetDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}