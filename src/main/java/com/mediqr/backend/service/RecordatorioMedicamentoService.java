package com.mediqr.backend.service;

import com.mediqr.backend.dto.RecordatorioMedicamentoCreateRequest;
import com.mediqr.backend.dto.RecordatorioMedicamentoUpdateRequest;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.RecordatorioMedicamento;
import com.mediqr.backend.repository.MedicamentoRepository;
import com.mediqr.backend.repository.RecordatorioMedicamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordatorioMedicamentoService {

    private final RecordatorioMedicamentoRepository recordatorioMedicamentoRepository;
    private final MedicamentoRepository medicamentoRepository;

    public RecordatorioMedicamentoService(
            RecordatorioMedicamentoRepository recordatorioMedicamentoRepository,
            MedicamentoRepository medicamentoRepository) {
        this.recordatorioMedicamentoRepository = recordatorioMedicamentoRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    public List<RecordatorioMedicamento> findAll() {
        return recordatorioMedicamentoRepository.findAll();
    }

    public Optional<RecordatorioMedicamento> findById(Long id) {
        return recordatorioMedicamentoRepository.findById(id);
    }

    public List<RecordatorioMedicamento> findByMedicamentoId(Long medicamentoId) {
        validateMedicamento(medicamentoId);
        return recordatorioMedicamentoRepository.findByMedicamentoId(medicamentoId);
    }

    public RecordatorioMedicamento getById(Long id) {
        return recordatorioMedicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recordatorio no encontrado"));
    }

    public RecordatorioMedicamento create(RecordatorioMedicamentoCreateRequest request) {
        validateMedicamento(request.getMedicamentoId());

        RecordatorioMedicamento recordatorio = new RecordatorioMedicamento();
        recordatorio.setMedicamentoId(request.getMedicamentoId());
        recordatorio.setHoraToma(request.getHoraToma());
        recordatorio.setMensaje(request.getMensaje());
        return recordatorioMedicamentoRepository.save(recordatorio);
    }

    public RecordatorioMedicamento update(Long id, RecordatorioMedicamentoUpdateRequest request) {
        RecordatorioMedicamento recordatorio = getById(id);
        recordatorio.setHoraToma(request.getHoraToma());
        recordatorio.setMensaje(request.getMensaje());
        if (request.getActivo() != null) {
            recordatorio.setActivo(request.getActivo());
        }
        return recordatorioMedicamentoRepository.save(recordatorio);
    }

    public void deleteById(Long id) {
        getById(id);
        recordatorioMedicamentoRepository.deleteById(id);
    }

    private void validateMedicamento(Long medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new ResourceNotFoundException("Medicamento no encontrado");
        }
    }
}
