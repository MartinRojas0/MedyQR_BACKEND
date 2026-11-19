package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotNull;

public class AutorizacionPacienteCreateRequest {

    @NotNull
    private Long personalId;

    public Long getPersonalId() {
        return personalId;
    }

    public void setPersonalId(Long personalId) {
        this.personalId = personalId;
    }
}