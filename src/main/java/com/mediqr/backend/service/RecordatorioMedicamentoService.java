package com.mediqr.backend.service;

import com.mediqr.backend.dto.RecordatorioMedicamentoCreateRequest;
import com.mediqr.backend.dto.RecordatorioMedicamentoUpdateRequest;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Medicamento;
import com.mediqr.backend.model.RecordatorioMedicamento;
import com.mediqr.backend.repository.MedicamentoRepository;
import com.mediqr.backend.repository.RecordatorioMedicamentoRepository;
import com.mediqr.backend.security.CurrentUserService;
import com.mediqr.backend.service.RegistroAccesoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordatorioMedicamentoService {

    private final RecordatorioMedicamentoRepository recordatorioMedicamentoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final RegistroAccesoService registroAccesoService;
    private final CurrentUserService currentUserService;

    public RecordatorioMedicamentoService(
            RecordatorioMedicamentoRepository recordatorioMedicamentoRepository,
            MedicamentoRepository medicamentoRepository,
            RegistroAccesoService registroAccesoService,
            CurrentUserService currentUserService) {
        this.recordatorioMedicamentoRepository = recordatorioMedicamentoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.registroAccesoService = registroAccesoService;
        this.currentUserService = currentUserService;
    }

    public List<RecordatorioMedicamento> findAll() {
        return recordatorioMedicamentoRepository.findAll();
    }

    public Optional<RecordatorioMedicamento> findById(Long id) {
        return recordatorioMedicamentoRepository.findById(id);
    }

    public List<RecordatorioMedicamento> findByMedicamentoId(Long medicamentoId) {
        validateMedicamento(medicamentoId);
        
        List<RecordatorioMedicamento> recordatorios = recordatorioMedicamentoRepository.findByMedicamentoId(medicamentoId);
        
        if (!recordatorios.isEmpty()) {
            Medicamento medicamento = medicamentoRepository.findById(medicamentoId).orElse(null);
            if (medicamento != null) {
                registrarAcceso(medicamento.getPacienteId(), "RECORDATORIO_MEDICAMENTO", "LECTURA_LISTA_RECORDATORIOS");
            }
        }

        return recordatorios;
    }

    public RecordatorioMedicamento getById(Long id) {
        RecordatorioMedicamento recordatorio = recordatorioMedicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recordatorio no encontrado"));

        Medicamento medicamento = medicamentoRepository.findById(recordatorio.getMedicamentoId()).orElse(null);
        if (medicamento != null) {
            registrarAcceso(medicamento.getPacienteId(), "RECORDATORIO_MEDICAMENTO", "LECTURA_RECORDATORIO");
        }

        return recordatorio;
    }

    public RecordatorioMedicamento create(RecordatorioMedicamentoCreateRequest request) {
        validateMedicamento(request.getMedicamentoId());

        RecordatorioMedicamento recordatorio = new RecordatorioMedicamento();
        recordatorio.setMedicamentoId(request.getMedicamentoId());
        recordatorio.setHoraToma(request.getHoraToma());
        recordatorio.setMensaje(request.getMensaje());
        
        RecordatorioMedicamento saved = recordatorioMedicamentoRepository.save(recordatorio);
        
        Medicamento medicamento = medicamentoRepository.findById(request.getMedicamentoId()).orElse(null);
        if (medicamento != null) {
            registrarAcceso(medicamento.getPacienteId(), "RECORDATORIO_MEDICAMENTO", "CREACION_RECORDATORIO");
        }

        return saved;
    }

    public RecordatorioMedicamento update(Long id, RecordatorioMedicamentoUpdateRequest request) {
        RecordatorioMedicamento recordatorio = getById(id);
        recordatorio.setHoraToma(request.getHoraToma());
        recordatorio.setMensaje(request.getMensaje());
        if (request.getActivo() != null) {
            recordatorio.setActivo(request.getActivo());
        }
        
        RecordatorioMedicamento saved = recordatorioMedicamentoRepository.save(recordatorio);
        
        Medicamento medicamento = medicamentoRepository.findById(recordatorio.getMedicamentoId()).orElse(null);
        if (medicamento != null) {
            registrarAcceso(medicamento.getPacienteId(), "RECORDATORIO_MEDICAMENTO", "ACTUALIZACION_RECORDATORIO");
        }

        return saved;
    }

    public void deleteById(Long id) {
        RecordatorioMedicamento recordatorio = getById(id);
        Medicamento medicamento = medicamentoRepository.findById(recordatorio.getMedicamentoId()).orElse(null);
        Long pacienteId = medicamento != null ? medicamento.getPacienteId() : null;
        
        recordatorioMedicamentoRepository.deleteById(id);
        
        if (pacienteId != null) {
            registrarAcceso(pacienteId, "RECORDATORIO_MEDICAMENTO", "ELIMINACION_RECORDATORIO");
        }
    }

    private void validateMedicamento(Long medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new ResourceNotFoundException("Medicamento no encontrado");
        }
    }

    private void registrarAcceso(Long pacienteId, String tipoAcceso, String accion) {
        if (pacienteId == null) return;
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser != null) {
            registroAccesoService.registrarAcceso(currentUser, pacienteId, tipoAcceso, accion, null);
        }
    }
}