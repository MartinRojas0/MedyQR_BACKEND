package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class RecordatorioMedicamentoCreateRequest {

    @NotNull
    private Long medicamentoId;

    @NotNull
    private LocalTime horaToma;

    private String mensaje;

    public Long getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(Long medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

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
}
