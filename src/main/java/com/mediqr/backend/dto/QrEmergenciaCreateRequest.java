package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotNull;

public class QrEmergenciaCreateRequest {

    @NotNull
    private Long pacienteId;

    private Boolean mostrarDni;
    private Boolean mostrarTipoSangre;
    private Boolean mostrarAlergias;
    private Boolean mostrarMedicamentos;
    private Boolean mostrarEnfermedades;
    private Boolean activo;

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
