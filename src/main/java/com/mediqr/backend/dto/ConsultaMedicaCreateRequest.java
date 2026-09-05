package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class ConsultaMedicaCreateRequest {

    @NotNull
    private Long pacienteId;

    @NotNull
    private Long personalId;

    @NotNull
    private OffsetDateTime fechaConsulta;

    private String motivo;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;

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

    public OffsetDateTime getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(OffsetDateTime fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
