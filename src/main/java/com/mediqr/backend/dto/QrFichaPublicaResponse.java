package com.mediqr.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record QrFichaPublicaResponse(
        String tipoSangre,
        String alergias,
        String enfermedadesCronicas,
        List<String> medicamentos,
        boolean informacionMedicaDisponible
) {
}
