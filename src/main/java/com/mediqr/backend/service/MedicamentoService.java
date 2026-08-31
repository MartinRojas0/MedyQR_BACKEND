package com.mediqr.backend.service;

import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.repository.MedicamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    public MedicamentoService(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    public List<Medicamento> findAll() {
        return medicamentoRepository.findAll();
    }

    public Optional<Medicamento> findById(Long id) {
        return medicamentoRepository.findById(id);
    }

    public Medicamento save(Medicamento medicamento) {
        return medicamentoRepository.save(medicamento);
    }

    public Optional<Medicamento> update(Long id, Medicamento medicamento) {
        return medicamentoRepository.findById(id).map(existingMedicamento -> {
            medicamento.setId(id);
            return medicamentoRepository.save(medicamento);
        });
    }

    public void deleteById(Long id) {
        medicamentoRepository.deleteById(id);
    }
}
