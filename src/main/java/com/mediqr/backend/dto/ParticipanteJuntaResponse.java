package com.mediqr.backend.dto;

import java.time.OffsetDateTime;

public class ParticipanteJuntaResponse {

    private Long id;
    private Long juntaId;
    private Long personalId;
    private String personalNombres;
    private String personalApellidos;
    private OffsetDateTime fechaIngreso;

    public ParticipanteJuntaResponse() {
    }

    public ParticipanteJuntaResponse(Long id, Long juntaId, Long personalId,
                                     String personalNombres, String personalApellidos,
                                     OffsetDateTime fechaIngreso) {
        this.id = id;
        this.juntaId = juntaId;
        this.personalId = personalId;
        this.personalNombres = personalNombres;
        this.personalApellidos = personalApellidos;
        this.fechaIngreso = fechaIngreso;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJuntaId() {
        return juntaId;
    }

    public void setJuntaId(Long juntaId) {
        this.juntaId = juntaId;
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

    public OffsetDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(OffsetDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}