package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class RecordatorioMedicamentoUpdateRequest {

    @NotNull
    private LocalTime horaToma;

    private String mensaje;
    private Boolean activo;

    public LocalTime getHoraToma() {
        return horaToma;
    }

    public void setHoraToma(LocalTime horaToma) {
        this.horaToma = horaToma;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
