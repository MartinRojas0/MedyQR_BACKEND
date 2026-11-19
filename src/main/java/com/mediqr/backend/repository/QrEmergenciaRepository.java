package com.mediqr.backend.repository;

import com.mediqr.backend.model.QrEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QrEmergenciaRepository extends JpaRepository<QrEmergencia, Long> {

	boolean existsByPacienteId(Long pacienteId);

	List<QrEmergencia> findByPacienteId(Long pacienteId);
}
