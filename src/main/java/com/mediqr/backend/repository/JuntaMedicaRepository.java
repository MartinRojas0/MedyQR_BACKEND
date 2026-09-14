package com.mediqr.backend.repository;

import com.mediqr.backend.model.JuntaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JuntaMedicaRepository extends JpaRepository<JuntaMedica, Long> {
    List<JuntaMedica> findByPacienteId(Long pacienteId);

    List<JuntaMedica> findByCreadorId(Long creadorId);

    List<JuntaMedica> findByEstadoAndFechaExpiracionAfter(String estado, OffsetDateTime now);

    Optional<JuntaMedica> findByIdAndCreadorId(Long id, Long creadorId);

    @Query("SELECT COUNT(j) > 0 FROM JuntaMedica j " +
           "JOIN ParticipanteJunta p ON p.juntaId = j.id " +
           "WHERE j.pacienteId = :pacienteId " +
           "AND p.personalId = :personalId " +
           "AND j.estado = 'ACTIVA' " +
           "AND j.fechaInicio <= CURRENT_TIMESTAMP " +
           "AND j.fechaExpiracion > CURRENT_TIMESTAMP")
    boolean existsActiveByPacienteIdAndParticipante(Long pacienteId, Long personalId);
}
