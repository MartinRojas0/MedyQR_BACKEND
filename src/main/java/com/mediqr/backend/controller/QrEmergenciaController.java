package com.mediqr.backend.controller;

import com.mediqr.backend.dto.QrEmergenciaCreateRequest;
import com.mediqr.backend.dto.QrEmergenciaEmergencyResponse;
import com.mediqr.backend.dto.QrEmergenciaResponse;
import com.mediqr.backend.dto.QrEmergenciaUpdateRequest;
import com.mediqr.backend.dto.QrFichaPublicaResponse;
import com.mediqr.backend.dto.QrIssueRequest;
import com.mediqr.backend.dto.QrIssueResponse;
import com.mediqr.backend.dto.QrPublicStateResponse;
import com.mediqr.backend.dto.QrPublicTokenRequest;
import com.mediqr.backend.dto.QrStatusResponse;
import com.mediqr.backend.exception.BusinessRuleException;
import com.mediqr.backend.exception.ResourceNotFoundException;
import com.mediqr.backend.service.QrEmergenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/qr-emergencia")
public class QrEmergenciaController {

    private final QrEmergenciaService qrEmergenciaService;

    @Value("${app.frontend.url}")
    private String publicFrontendUrl;

    public QrEmergenciaController(QrEmergenciaService qrEmergenciaService) {
        this.qrEmergenciaService = qrEmergenciaService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PACIENTE')")
    public QrStatusResponse getMyStatus() {
        return qrEmergenciaService.statusForCurrentPatient();
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<QrIssueResponse> issueForMe(@RequestBody QrIssueRequest request) {
        QrIssueResponse issued = qrEmergenciaService.issueForCurrentPatient(request, publicFrontendUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(issued);
    }

    @PostMapping("/me/image")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<byte[]> downloadMyImage(@Valid @RequestBody QrPublicTokenRequest request) {
        byte[] image = qrEmergenciaService.generateImageForCurrentPatient(request.token(), publicFrontendUrl);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"mediqr-emergencia.png\"")
                .header("Referrer-Policy", "no-referrer")
                .body(image);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> revokeMyQr() {
        qrEmergenciaService.revokeForCurrentPatient();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/public/resolve")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> resolvePublicToken(@Valid @RequestBody QrPublicTokenRequest request) {
        QrEmergenciaService.PublicResolution result = qrEmergenciaService.resolvePublicToken(request);
        if ("invalid".equals(result.state())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .cacheControl(CacheControl.noStore())
                    .header("Referrer-Policy", "no-referrer")
                    .body(new QrPublicStateResponse("invalid"));
        }
        if ("revoked".equals(result.state()) || "expired".equals(result.state())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .cacheControl(CacheControl.noStore())
                    .header("Referrer-Policy", "no-referrer")
                    .body(new QrPublicStateResponse(result.state()));
        }
        QrFichaPublicaResponse response = result.response();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .body(response);
    }

    @GetMapping("/emergency/{token}")
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public ResponseEntity<QrEmergenciaEmergencyResponse> accessByToken(@PathVariable UUID token) {
        try {
            QrEmergenciaEmergencyResponse response = qrEmergenciaService.accessByToken(token);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessRuleException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('PERSONAL_SALUD')")
    public List<QrEmergenciaResponse> getAll() {
        throw new AccessDeniedException("La lista global de códigos QR no está disponible");
    }

    @GetMapping("/{id}")
    public ResponseEntity<QrEmergenciaResponse> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(qrEmergenciaService.getById(id));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<QrEmergenciaResponse> getByPaciente(@PathVariable Long pacienteId) {
        return qrEmergenciaService.findByPacienteId(pacienteId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<QrEmergenciaResponse> create(@Valid @RequestBody QrEmergenciaCreateRequest request) {
        QrEmergenciaResponse saved = qrEmergenciaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<QrEmergenciaResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody QrEmergenciaUpdateRequest request) {
        try {
            return ResponseEntity.ok(qrEmergenciaService.update(id, request));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            qrEmergenciaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}