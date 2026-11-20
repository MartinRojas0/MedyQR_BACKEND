package com.mediqr.backend.controller;

import com.mediqr.backend.dto.DocumentoResponse;
import com.mediqr.backend.service.DocumentoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/documentos/me")
@PreAuthorize("hasRole('PACIENTE')")
@ConditionalOnProperty(name = "mediqr.documents.enabled", havingValue = "true")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping
    public List<DocumentoResponse> listMine() {
        return documentoService.listMine();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponse> uploadMine(
            @RequestParam("archivo") MultipartFile file,
            @RequestParam("tipo") String tipo,
            @RequestParam("fechaDocumento") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDocumento,
            @RequestParam(value = "citaId", required = false) Long citaId) throws IOException {
        return ResponseEntity.status(201)
                .body(documentoService.uploadMine(file, tipo, fechaDocumento, citaId));
    }

    @GetMapping("/{id}/contenido")
    public ResponseEntity<byte[]> content(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean descargar)
            throws IOException {
        DocumentoService.DocumentContent content = documentoService.contentMine(id);
        ContentDisposition disposition = (descargar
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename(content.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .contentLength(content.bytes().length)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(content.bytes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMine(@PathVariable Long id) throws IOException {
        documentoService.deleteMine(id);
        return ResponseEntity.noContent().build();
    }
}
