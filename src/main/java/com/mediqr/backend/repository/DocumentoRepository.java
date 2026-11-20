package com.mediqr.backend.repository;

import com.mediqr.backend.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findAllByPacienteIdOrderByFechaDocumentoDescCreatedAtDesc(Long pacienteId);

    Optional<Documento> findByIdAndPacienteId(Long id, Long pacienteId);
}
