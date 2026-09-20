package com.mediqr.backend.repository;

import com.mediqr.backend.model.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {

	boolean existsByPacienteId(Long pacienteId);
	Optional<HistorialClinico> findByPacienteId(Long pacienteId);
}
