package com.mediqr.backend.dto;

import java.time.OffsetDateTime;

public class AutorizacionPacienteResponse {

    private Long id;
    private Long pacienteId;
    private Long personalId;
    private String personalNombres;
    private String personalApellidos;
    private String estado;
    private OffsetDateTime fechaAutorizacion;
    private OffsetDateTime fechaRevocacion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public AutorizacionPacienteResponse() {
    }

    public AutorizacionPacienteResponse(Long id, Long pacienteId, Long personalId, String personalNombres, String personalApellidos, String estado, OffsetDateTime fechaAutorizacion, OffsetDateTime fechaRevocacion, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.personalId = personalId;
        this.personalNombres = personalNombres;
        this.personalApellidos = personalApellidos;
        this.estado = estado;
        this.fechaAutorizacion = fechaAutorizacion;
        this.fechaRevocacion = fechaRevocacion;
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

    public Long getPersonalId() {
        return personalId;
    }

    public void setPersonalId(Long personalId) {
        this.personalId = personalId;
    }

    public String getPersonalNombres() {
        return personalNombres;
    }

    public void setPersonalNombres(String personalNombres) {
        this.personalNombres = personalNombres;
    }

    public String getPersonalApellidos() {
        return personalApellidos;
    }

    public void setPersonalApellidos(String personalApellidos) {
        this.personalApellidos = personalApellidos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(OffsetDateTime fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public OffsetDateTime getFechaRevocacion() {
        return fechaRevocacion;
    }

    public void setFechaRevocacion(OffsetDateTime fechaRevocacion) {
        this.fechaRevocacion = fechaRevocacion;
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