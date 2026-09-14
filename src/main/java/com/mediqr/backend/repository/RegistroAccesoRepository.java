package com.mediqr.backend.repository;

import com.mediqr.backend.model.RegistroAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long> {
    List<RegistroAcceso> findByPacienteId(Long pacienteId);

    List<RegistroAcceso> findByPersonalId(Long personalId);
}
