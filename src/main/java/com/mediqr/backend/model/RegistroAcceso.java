package com.mediqr.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "registro_accesos")
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id")
    private Long pacienteId;

    @Column(name = "personal_id")
    private Long personalId;

    @Column(name = "tipo_acceso")
    private String tipoAcceso;

    private String accion;

    @Column(name = "fecha_acceso")
    private OffsetDateTime fechaAcceso;

    @Column(name = "identificador_qr")
    private UUID identificadorQr;

    public RegistroAcceso() {
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

    public Long getPersonalId() {
        return personalId;
    }

    public void setPersonalId(Long personalId) {
        this.personalId = personalId;
    }

    public String getTipoAcceso() {
        return tipoAcceso;
    }

    public void setTipoAcceso(String tipoAcceso) {
        this.tipoAcceso = tipoAcceso;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public OffsetDateTime getFechaAcceso() {
        return fechaAcceso;
    }

    public void setFechaAcceso(OffsetDateTime fechaAcceso) {
        this.fechaAcceso = fechaAcceso;
    }

    public UUID getIdentificadorQr() {
        return identificadorQr;
    }

    public void setIdentificadorQr(UUID identificadorQr) {
        this.identificadorQr = identificadorQr;
    }
}
