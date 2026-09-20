package com.mediqr.backend.repository;

import com.mediqr.backend.model.QrEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QrEmergenciaRepository extends JpaRepository<QrEmergencia, Long> {

	boolean existsByPacienteId(Long pacienteId);

	List<QrEmergencia> findByPacienteId(Long pacienteId);

	Optional<QrEmergencia> findByToken(UUID token);
}
