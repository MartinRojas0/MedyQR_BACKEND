package com.mediqr.backend.service;

import com.mediqr.backend.exception.InvalidDocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "MEDIQR_DOCUMENT_STORAGE_DIR")
public class DocumentoStorageService {

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;
    private final Path storageRoot;

    public DocumentoStorageService(
            @Value("${MEDIQR_DOCUMENT_STORAGE_DIR}") String storageDirectory) {
        if (storageDirectory.isBlank()) {
            throw new IllegalStateException("MEDIQR_DOCUMENT_STORAGE_DIR must not be blank");
        }
        try {
            this.storageRoot = Path.of(storageDirectory).toAbsolutePath().normalize();
        } catch (InvalidPathException exception) {
            throw new IllegalStateException("The document storage directory is invalid", exception);
        }
    }

    public StoredFile store(MultipartFile upload) throws IOException {
        if (upload == null || upload.isEmpty()) {
            throw new InvalidDocumentException("Selecciona un archivo con contenido.");
        }
        if (upload.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidDocumentException("El archivo supera el límite de 10 MB.");
        }

        String fileName = safeFileName(upload.getOriginalFilename());
        String extension = extensionOf(fileName);
        byte[] content = upload.getBytes();
        String contentType = validateAndDetectType(extension, content);
        String storageKey = UUID.randomUUID() + "." + extension;

        Files.createDirectories(storageRoot);
        Files.write(resolve(storageKey), content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return new StoredFile(fileName, storageKey, contentType, content.length);
    }

    public byte[] read(String storageKey) throws IOException {
        return Files.readAllBytes(resolve(storageKey));
    }

    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(resolve(storageKey));
    }

    private Path resolve(String storageKey) {
        if (storageKey == null || !storageKey.matches(
                "^[0-9a-fA-F-]{36}\\.(pdf|jpg|jpeg|png)$")) {
            throw new IllegalStateException("Stored document key is invalid");
        }
        Path target = storageRoot.resolve(storageKey).normalize();
        if (!target.startsWith(storageRoot)) {
            throw new IllegalStateException("Stored document key escapes its storage directory");
        }
        return target;
    }

    private String safeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            throw new InvalidDocumentException("El archivo debe tener un nombre válido.");
        }
        String normalized = originalName.replace('\\', '/');
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (fileName.isBlank() || fileName.length() > 255
                || fileName.chars().anyMatch(Character::isISOControl)) {
            throw new InvalidDocumentException("El nombre del archivo no es válido.");
        }
        return fileName;
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 1 || dot == fileName.length() - 1) {
            throw new InvalidDocumentException("Solo se aceptan archivos PDF, JPG y PNG.");
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!extension.matches("pdf|jpg|jpeg|png")) {
            throw new InvalidDocumentException("Solo se aceptan archivos PDF, JPG y PNG.");
        }
        return extension;
    }

    private String validateAndDetectType(String extension, byte[] bytes) {
        boolean pdf = startsWith(bytes, new byte[]{'%', 'P', 'D', 'F', '-'});
        boolean png = startsWith(bytes, new byte[]{
                (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
        boolean jpeg = startsWith(bytes, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

        if ("pdf".equals(extension) && pdf) return "application/pdf";
        if ("png".equals(extension) && png) return "image/png";
        if (("jpg".equals(extension) || "jpeg".equals(extension)) && jpeg) return "image/jpeg";
        throw new InvalidDocumentException("El contenido no coincide con la extensión PDF, JPG o PNG.");
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) return false;
        }
        return true;
    }

    public record StoredFile(String fileName, String storageKey, String contentType, long sizeBytes) {
    }
}
