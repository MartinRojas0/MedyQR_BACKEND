package com.mediqr.backend.dto;

public record QrIssueRequest(
        Boolean mostrarTipoSangre,
        Boolean mostrarAlergias,
        Boolean mostrarMedicamentos,
        Boolean mostrarEnfermedades
) {
}
