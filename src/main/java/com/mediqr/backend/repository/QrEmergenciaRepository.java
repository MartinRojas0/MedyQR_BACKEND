package com.mediqr.backend.repository;

import com.mediqr.backend.model.QrEmergencia;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QrEmergenciaRepository extends JpaRepository<QrEmergencia, Long> {

	boolean existsByPacienteId(Long pacienteId);

	List<QrEmergencia> findByPacienteId(Long pacienteId);

	Optional<QrEmergencia> findByToken(UUID token);

	Optional<QrEmergencia> findByTokenHash(String tokenHash);

	@Modifying
	@Transactional
	@Query("update QrEmergencia q set q.activo = false, q.revokedAt = :revokedAt "
			+ "where q.pacienteId = :pacienteId and q.activo = true")
	int revokeActiveForPatient(@Param("pacienteId") Long pacienteId,
							   @Param("revokedAt") java.time.OffsetDateTime revokedAt);
}
