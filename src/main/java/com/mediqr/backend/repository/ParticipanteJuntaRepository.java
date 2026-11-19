package com.mediqr.backend.repository;

import com.mediqr.backend.model.ParticipanteJunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipanteJuntaRepository extends JpaRepository<ParticipanteJunta, Long> {
    List<ParticipanteJunta> findByJuntaId(Long juntaId);

    List<ParticipanteJunta> findByPersonalId(Long personalId);

    Optional<ParticipanteJunta> findByJuntaIdAndPersonalId(Long juntaId, Long personalId);

    boolean existsByJuntaIdAndPersonalId(Long juntaId, Long personalId);
}
