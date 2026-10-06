package com.mediqr.backend.repository;

import com.mediqr.backend.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
boolean existsByUsuarioId(Long usuarioId);
boolean existsByDocumentoIdentidad(String documentoIdentidad);
boolean existsByDocumentoIdentidadAndIdNot(String documentoIdentidad, Long id);
}
