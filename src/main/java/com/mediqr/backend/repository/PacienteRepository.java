package com.mediqr.backend.repository;

import com.mediqr.backend.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    boolean existsByDocumentoIdentidadAndIdNot(String documentoIdentidad, Long id);
    Optional<Paciente> findByUsuarioId(Long usuarioId);
}
