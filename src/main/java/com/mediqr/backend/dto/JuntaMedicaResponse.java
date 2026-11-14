package com.mediqr.backend.dto;

import java.time.OffsetDateTime;

public class JuntaMedicaResponse {

    private Long id;
    private Long pacienteId;
    private Long creadorId;
    private String motivo;
    private OffsetDateTime fechaInicio;
    private OffsetDateTime fechaExpiracion;
    private String estado;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public JuntaMedicaResponse() {
    }

    public JuntaMedicaResponse(Long id, Long pacienteId, Long creadorId, String motivo,
                               OffsetDateTime fechaInicio, OffsetDateTime fechaExpiracion,
                               String estado, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.creadorId = creadorId;
        this.motivo = motivo;
        this.fechaInicio = fechaInicio;
        this.fechaExpiracion = fechaExpiracion;
        this.estado = estado;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getCreadorId() {
        return creadorId;
    }

    public void setCreadorId(Long creadorId) {
        this.creadorId = creadorId;
    }

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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}