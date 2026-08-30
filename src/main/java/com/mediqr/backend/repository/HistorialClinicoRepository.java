package com.mediqr.backend.repository;

import com.mediqr.backend.model.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {
}
