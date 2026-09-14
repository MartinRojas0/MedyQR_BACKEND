package com.mediqr.backend.repository;

import com.mediqr.backend.model.AutorizacionPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutorizacionPacienteRepository extends JpaRepository<AutorizacionPaciente, Long> {
    boolean existsByPacienteIdAndPersonalIdAndEstado(Long pacienteId, Long personalId, String estado);

    List<AutorizacionPaciente> findByPacienteId(Long pacienteId);

    List<AutorizacionPaciente> findByPersonalId(Long personalId);

    Optional<AutorizacionPaciente> findByPacienteIdAndPersonalId(Long pacienteId, Long personalId);
}
