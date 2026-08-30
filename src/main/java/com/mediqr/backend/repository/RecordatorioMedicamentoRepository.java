package com.mediqr.backend.repository;

import com.mediqr.backend.model.RecordatorioMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordatorioMedicamentoRepository extends JpaRepository<RecordatorioMedicamento, Long> {
}
