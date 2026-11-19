package com.mediqr.backend.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class QrEmergenciaEmergencyResponse {

    private Long pacienteId;
    private String nombres;
    private String apellidos;
    private String documentoIdentidad;
    private LocalDate fechaNacimiento;
    private String tipoSangre;
    private String alergias;
    private String enfermedadesCronicas;
    private String medicamentos;
    private OffsetDateTime fechaAcceso;

    public QrEmergenciaEmergencyResponse() {
    }

    public QrEmergenciaEmergencyResponse(Long pacienteId, String nombres, String apellidos,
                                         String documentoIdentidad, LocalDate fechaNacimiento,
                                         String tipoSangre, String alergias,
                                         String enfermedadesCronicas, String medicamentos,
                                         OffsetDateTime fechaAcceso) {
        this.pacienteId = pacienteId;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.documentoIdentidad = documentoIdentidad;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoSangre = tipoSangre;
        this.alergias = alergias;
        this.enfermedadesCronicas = enfermedadesCronicas;
        this.medicamentos = medicamentos;
        this.fechaAcceso = fechaAcceso;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTipoSangre() {
        return tipoSangre;
    }

    public void setTipoSangre(String tipoSangre) {
        this.tipoSangre = tipoSangre;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getEnfermedadesCronicas() {
        return enfermedadesCronicas;
    }

    public void setEnfermedadesCronicas(String enfermedadesCronicas) {
        this.enfermedadesCronicas = enfermedadesCronicas;
    }

    public String getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(String medicamentos) {
        this.medicamentos = medicamentos;
    }

    public OffsetDateTime getFechaAcceso() {
        return fechaAcceso;
    }

    public void setFechaAcceso(OffsetDateTime fechaAcceso) {
        this.fechaAcceso = fechaAcceso;
    }
}