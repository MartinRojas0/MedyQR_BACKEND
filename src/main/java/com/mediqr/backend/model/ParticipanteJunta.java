package com.mediqr.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "participantes_junta")
public class ParticipanteJunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "junta_id")
    private Long juntaId;

    @Column(name = "personal_id")
    private Long personalId;

    @Column(name = "fecha_ingreso")
    private OffsetDateTime fechaIngreso;

    public ParticipanteJunta() {
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

    public OffsetDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(OffsetDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}
