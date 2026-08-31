package com.mediqr.backend.service;

import com.mediqr.backend.model.RecordatorioMedicamento;
import com.mediqr.backend.repository.RecordatorioMedicamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordatorioMedicamentoService {

    private final RecordatorioMedicamentoRepository recordatorioMedicamentoRepository;

    public RecordatorioMedicamentoService(RecordatorioMedicamentoRepository recordatorioMedicamentoRepository) {
        this.recordatorioMedicamentoRepository = recordatorioMedicamentoRepository;
    }

    public List<RecordatorioMedicamento> findAll() {
        return recordatorioMedicamentoRepository.findAll();
    }

    public Optional<RecordatorioMedicamento> findById(Long id) {
        return recordatorioMedicamentoRepository.findById(id);
    }

    public RecordatorioMedicamento save(RecordatorioMedicamento recordatorioMedicamento) {
        return recordatorioMedicamentoRepository.save(recordatorioMedicamento);
    }

    public Optional<RecordatorioMedicamento> update(Long id, RecordatorioMedicamento recordatorioMedicamento) {
        return recordatorioMedicamentoRepository.findById(id).map(existingRecordatorioMedicamento -> {
            recordatorioMedicamento.setId(id);
            return recordatorioMedicamentoRepository.save(recordatorioMedicamento);
        });
    }

    public void deleteById(Long id) {
        recordatorioMedicamentoRepository.deleteById(id);
    }
}
