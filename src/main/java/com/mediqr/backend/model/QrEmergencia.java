package com.mediqr.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "qr_emergencia")
public class QrEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id")
    private Long pacienteId;

    private UUID token = UUID.randomUUID();

    @Column(name = "mostrar_dni")
    private Boolean mostrarDni = true;

    @Column(name = "mostrar_tipo_sangre")
    private Boolean mostrarTipoSangre = true;

    @Column(name = "mostrar_alergias")
    private Boolean mostrarAlergias = true;

    @Column(name = "mostrar_medicamentos")
    private Boolean mostrarMedicamentos = true;

    @Column(name = "mostrar_enfermedades")
    private Boolean mostrarEnfermedades = true;

    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public QrEmergencia() {
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

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
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
