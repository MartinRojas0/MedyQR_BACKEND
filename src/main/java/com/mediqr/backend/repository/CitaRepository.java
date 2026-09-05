package com.mediqr.backend.repository;

import com.mediqr.backend.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface CitaRepository extends JpaRepository<Cita, Long> {

	boolean existsByPersonalIdAndFechaHora(Long personalId, OffsetDateTime fechaHora);

	boolean existsByPacienteIdAndFechaHora(Long pacienteId, OffsetDateTime fechaHora);

	boolean existsByPersonalIdAndFechaHoraAndIdNot(Long personalId, OffsetDateTime fechaHora, Long id);

	boolean existsByPacienteIdAndFechaHoraAndIdNot(Long pacienteId, OffsetDateTime fechaHora, Long id);
}
