package com.mediqr.backend.dto;

import jakarta.validation.constraints.NotNull;

public class ParticipanteJuntaCreateRequest {

    @NotNull
    private Long personalId;

    public Long getPersonalId() {
        return personalId;
    }

    public void setPersonalId(Long personalId) {
        this.personalId = personalId;
    }
}