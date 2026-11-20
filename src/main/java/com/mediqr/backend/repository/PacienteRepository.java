package com.mediqr.backend.repository;

import com.mediqr.backend.model.Paciente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    boolean existsByDocumentoIdentidadAndIdNot(String documentoIdentidad, Long id);
    Optional<Paciente> findByUsuarioId(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Paciente p where p.id = :id")
    Optional<Paciente> findByIdForUpdate(@Param("id") Long id);
}
