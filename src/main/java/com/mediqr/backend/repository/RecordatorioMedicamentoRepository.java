package com.mediqr.backend.repository;

import com.mediqr.backend.model.RecordatorioMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordatorioMedicamentoRepository extends JpaRepository<RecordatorioMedicamento, Long> {

	List<RecordatorioMedicamento> findByMedicamentoId(Long medicamentoId);
}
