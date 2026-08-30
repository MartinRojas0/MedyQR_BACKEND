package com.mediqr.backend.repository;

import com.mediqr.backend.model.AutorizacionPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorizacionPacienteRepository extends JpaRepository<AutorizacionPaciente, Long> {
}
