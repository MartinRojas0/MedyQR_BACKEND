package com.mediqr.backend.service;

import com.mediqr.backend.dto.DocumentoResponse;
import com.mediqr.backend.exception.InvalidDocumentException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.model.Cita;
import com.mediqr.backend.model.Documento;
import com.mediqr.backend.repository.CitaRepository;
import com.mediqr.backend.repository.DocumentoRepository;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class DocumentoService {

    private static final List<String> TIPOS = List.of("Receta", "Examen", "Informe", "Otro");

    private final DocumentoRepository documentoRepository;
    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final DocumentoStorageService storageService;
    private final CurrentUserService currentUserService;

    public DocumentoService(DocumentoRepository documentoRepository,
                            PacienteRepository pacienteRepository,
                            CitaRepository citaRepository,
                            DocumentoStorageService storageService,
                            CurrentUserService currentUserService) {
        this.documentoRepository = documentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.storageService = storageService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> listMine() {
        Long pacienteId = requirePatientId();
        return documentoRepository.findAllByPacienteIdOrderByFechaDocumentoDescCreatedAtDesc(pacienteId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public DocumentoResponse uploadMine(MultipartFile file, String tipo, LocalDate fechaDocumento, Long citaId)
            throws IOException {
        Long pacienteId = requirePatientId();
        if (!TIPOS.contains(tipo)) {
            throw new InvalidDocumentException("El tipo de documento no es válido.");
        }
        if (fechaDocumento == null || fechaDocumento.isAfter(LocalDate.now())) {
            throw new InvalidDocumentException("La fecha del documento no puede estar en el futuro.");
        }
        validateAppointmentOwnership(citaId, pacienteId);

        DocumentoStorageService.StoredFile stored = storageService.store(file);
        Documento documento = new Documento();
        documento.setPacienteId(pacienteId);
        documento.setCitaId(citaId);
        documento.setNombreArchivo(stored.fileName());
        documento.setStorageKey(stored.storageKey());
        documento.setTipo(tipo);
        documento.setContentType(stored.contentType());
        documento.setSizeBytes(stored.sizeBytes());
        documento.setFechaDocumento(fechaDocumento);
        try {
            return toResponse(documentoRepository.save(documento));
        } catch (RuntimeException exception) {
            storageService.delete(stored.storageKey());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public DocumentContent contentMine(Long id) throws IOException {
        Documento documento = findOwned(id);
        return new DocumentContent(
                documento.getNombreArchivo(),
                documento.getContentType(),
                storageService.read(documento.getStorageKey()));
    }

    @Transactional
    public void deleteMine(Long id) throws IOException {
        Documento documento = findOwned(id);
        storageService.delete(documento.getStorageKey());
        documentoRepository.delete(documento);
    }

    private Documento findOwned(Long id) {
        Long pacienteId = requirePatientId();
        return documentoRepository.findByIdAndPacienteId(id, pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));
    }

    private Long requirePatientId() {
        var currentUser = currentUserService.getCurrentUser();
        if (currentUser == null || !"PACIENTE".equals(currentUser.rol())
                || !Boolean.TRUE.equals(currentUser.activo())) {
            throw new AccessDeniedException("Solo un paciente activo puede gestionar sus documentos");
        }
        return pacienteRepository.findByUsuarioId(currentUser.userId())
                .filter(paciente -> Boolean.TRUE.equals(paciente.getActivo()))
                .map(paciente -> paciente.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado"));
    }

    private void validateAppointmentOwnership(Long citaId, Long pacienteId) {
        if (citaId == null) return;
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        if (!pacienteId.equals(cita.getPacienteId())) {
            throw new AccessDeniedException("No puedes relacionar un documento con la cita de otro paciente");
        }
    }

    private DocumentoResponse toResponse(Documento documento) {
        return new DocumentoResponse(
                documento.getId(),
                documento.getNombreArchivo(),
                documento.getTipo(),
                documento.getFechaDocumento(),
                documento.getCitaId(),
                documento.getContentType(),
                documento.getSizeBytes(),
                documento.getCreatedAt());
    }

    public record DocumentContent(String fileName, String contentType, byte[] bytes) {
    }
}
