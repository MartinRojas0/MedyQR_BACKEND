package com.mediqr.backend.dto;

import java.time.OffsetDateTime;

public class QrEmergenciaResponse {

    private Long id;
    private Long pacienteId;
    private Boolean activo;
    private Boolean mostrarDni;
    private Boolean mostrarTipoSangre;
    private Boolean mostrarAlergias;
    private Boolean mostrarMedicamentos;
    private Boolean mostrarEnfermedades;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public QrEmergenciaResponse() {
    }

    public QrEmergenciaResponse(Long id, Long pacienteId, Boolean activo, Boolean mostrarDni,
                                Boolean mostrarTipoSangre, Boolean mostrarAlergias,
                                Boolean mostrarMedicamentos, Boolean mostrarEnfermedades,
                                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.activo = activo;
        this.mostrarDni = mostrarDni;
        this.mostrarTipoSangre = mostrarTipoSangre;
        this.mostrarAlergias = mostrarAlergias;
        this.mostrarMedicamentos = mostrarMedicamentos;
        this.mostrarEnfermedades = mostrarEnfermedades;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getMostrarDni() {
        return mostrarDni;
    }

    public void setMostrarDni(Boolean mostrarDni) {
        this.mostrarDni = mostrarDni;
    }

    public Boolean getMostrarTipoSangre() {
        return mostrarTipoSangre;
    }

    public void setMostrarTipoSangre(Boolean mostrarTipoSangre) {
        this.mostrarTipoSangre = mostrarTipoSangre;
    }

    public Boolean getMostrarAlergias() {
        return mostrarAlergias;
    }

    public void setMostrarAlergias(Boolean mostrarAlergias) {
        this.mostrarAlergias = mostrarAlergias;
    }

    public Boolean getMostrarMedicamentos() {
        return mostrarMedicamentos;
    }

    public void setMostrarMedicamentos(Boolean mostrarMedicamentos) {
        this.mostrarMedicamentos = mostrarMedicamentos;
    }

    public Boolean getMostrarEnfermedades() {
        return mostrarEnfermedades;
    }

    public void setMostrarEnfermedades(Boolean mostrarEnfermedades) {
        this.mostrarEnfermedades = mostrarEnfermedades;
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