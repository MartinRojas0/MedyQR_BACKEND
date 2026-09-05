package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CitaEstadoRequest {

    @NotBlank
    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
