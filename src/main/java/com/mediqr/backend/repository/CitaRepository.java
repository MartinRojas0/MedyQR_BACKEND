package com.mediqr.backend.repository;

import com.mediqr.backend.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.LockModeType;

public interface CitaRepository extends JpaRepository<Cita, Long> {

	boolean existsByPersonalIdAndFechaHora(Long personalId, OffsetDateTime fechaHora);

	boolean existsByPacienteIdAndFechaHora(Long pacienteId, OffsetDateTime fechaHora);

	boolean existsByPersonalIdAndFechaHoraAndIdNot(Long personalId, OffsetDateTime fechaHora, Long id);

	boolean existsByPacienteIdAndFechaHoraAndIdNot(Long pacienteId, OffsetDateTime fechaHora, Long id);

	boolean existsByPersonalIdAndFechaHoraAndEstadoIn(
			Long personalId, OffsetDateTime fechaHora, Set<String> estados);

	boolean existsByPacienteIdAndFechaHoraAndEstadoIn(
			Long pacienteId, OffsetDateTime fechaHora, Set<String> estados);

	boolean existsByPersonalIdAndFechaHoraAndEstadoInAndIdNot(
			Long personalId, OffsetDateTime fechaHora, Set<String> estados, Long id);

	boolean existsByPacienteIdAndFechaHoraAndEstadoInAndIdNot(
			Long pacienteId, OffsetDateTime fechaHora, Set<String> estados, Long id);

	List<Cita> findAllByPacienteIdOrderByFechaHoraAsc(Long pacienteId);

	Optional<Cita> findByIdAndPacienteId(Long id, Long pacienteId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Cita c where c.id = :id and c.pacienteId = :pacienteId")
	Optional<Cita> findByIdAndPacienteIdForUpdate(
			@Param("id") Long id, @Param("pacienteId") Long pacienteId);
}
