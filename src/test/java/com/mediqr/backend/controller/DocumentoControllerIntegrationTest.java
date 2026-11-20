package com.mediqr.backend.controller;

import com.mediqr.backend.repository.DocumentoRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import com.mediqr.backend.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentoControllerIntegrationTest {

    private static final String PASSWORD = "test-password";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final byte[] PDF = "%PDF-1.4\nMediQR test file\n".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void documentsPersistAndAreScopedToTheAuthenticatedPatient() throws Exception {
        PatientSession owner = registerAndLogin("docs.owner@example.test", "docs_owner");
        PatientSession other = registerAndLogin("docs.other@example.test", "docs_other");

        mockMvc.perform(get("/api/documentos/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/documentos/me")
                        .header("Authorization", other.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        MockMultipartFile upload = new MockMultipartFile(
                "archivo", "../../receta.pdf", "application/pdf", PDF);
        MvcResult uploaded = mockMvc.perform(multipart("/api/documentos/me")
                        .file(upload)
                        .param("tipo", "Receta")
                        .param("fechaDocumento", LocalDate.now().minusDays(1).toString())
                        .header("Authorization", owner.authorization()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreArchivo").value("receta.pdf"))
                .andExpect(jsonPath("$.tipo").value("Receta"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.sizeBytes").value(PDF.length))
                .andReturn();
        long documentId = longField(uploaded.getResponse().getContentAsString(), "id");

        mockMvc.perform(get("/api/documentos/me")
                        .header("Authorization", owner.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(documentId));

        PatientSession ownerAfterRelogin = login("docs.owner@example.test", "docs_owner");
        MvcResult downloaded = mockMvc.perform(get("/api/documentos/me/{id}/contenido", documentId)
                        .queryParam("descargar", "true")
                        .header("Authorization", ownerAfterRelogin.authorization()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andReturn();
        assertArrayEquals(PDF, downloaded.getResponse().getContentAsByteArray());
        assertTrue(downloaded.getResponse().getHeader("Content-Disposition").contains("attachment"));

        mockMvc.perform(get("/api/documentos/me/{id}/contenido", documentId)
                        .header("Authorization", other.authorization()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/documentos/me/{id}", documentId)
                        .header("Authorization", other.authorization()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/documentos/me")
                        .header("Authorization", other.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(delete("/api/documentos/me/{id}", documentId)
                        .header("Authorization", ownerAfterRelogin.authorization()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/documentos/me")
                        .header("Authorization", ownerAfterRelogin.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        org.junit.jupiter.api.Assertions.assertFalse(documentoRepository.existsById(documentId));
    }

    @Test
    void invalidFileContentsAndDocumentTypesAreRejected() throws Exception {
        PatientSession patient = registerAndLogin("docs.invalid@example.test", "docs_invalid");

        mockMvc.perform(multipart("/api/documentos/me")
                        .file(new MockMultipartFile(
                                "archivo", "spoofed.pdf", "application/pdf", "not a pdf".getBytes(StandardCharsets.US_ASCII)))
                        .param("tipo", "Receta")
                        .param("fechaDocumento", LocalDate.now().minusDays(1).toString())
                        .header("Authorization", patient.authorization()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(multipart("/api/documentos/me")
                        .file(new MockMultipartFile("archivo", "valid.pdf", "application/pdf", PDF))
                        .param("tipo", "Documento médico")
                        .param("fechaDocumento", LocalDate.now().minusDays(1).toString())
                        .header("Authorization", patient.authorization()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/documentos/me")
                        .header("Authorization", patient.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void healthStaffCannotAccessPatientDocumentRoutes() throws Exception {
        Usuario staff = new Usuario();
        staff.setEmail("docs.staff@example.test");
        staff.setUsername("docs_staff");
        staff.setPasswordHash(passwordEncoder.encode(PASSWORD));
        staff.setRol("PERSONAL_SALUD");
        staff.setActivo(true);
        usuarioRepository.save(staff);
        PatientSession staffSession = login(staff.getEmail(), staff.getUsername());

        mockMvc.perform(get("/api/documentos/me")
                        .header("Authorization", staffSession.authorization()))
                .andExpect(status().isForbidden());
    }

    @Test
    void oversizedUploadsAreRejectedWithoutCreatingDocumentRecords() throws Exception {
        PatientSession patient = registerAndLogin("docs.large@example.test", "docs_large");
        byte[] oversizedPdf = new byte[MAX_FILE_SIZE + 1];
        System.arraycopy(PDF, 0, oversizedPdf, 0, PDF.length);

        mockMvc.perform(multipart("/api/documentos/me")
                        .file(new MockMultipartFile(
                                "archivo", "large.pdf", "application/pdf", oversizedPdf))
                        .param("tipo", "Informe")
                        .param("fechaDocumento", LocalDate.now().minusDays(1).toString())
                        .header("Authorization", patient.authorization()))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/documentos/me")
                        .header("Authorization", patient.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private PatientSession registerAndLogin(String email, String username) throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"email\":\"%s\",\"username\":\"%s\",\"password\":\"%s\"}",
                                email, username, PASSWORD)))
                .andExpect(status().isCreated());
        return login(email, username);
    }

    private PatientSession login(String email, String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"identifier\":\"%s\",\"password\":\"%s\"}", username, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return new PatientSession("Bearer " + stringField(result.getResponse().getContentAsString(), "token"));
    }

    private long longField(String json, String name) {
        return Long.parseLong(fieldValue(json, name, "(\\d+)"));
    }

    private String stringField(String json, String name) {
        return fieldValue(json, name, "\"([^\"]+)\"");
    }

    private String fieldValue(String json, String name, String valuePattern) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + name + "\"\\s*:\\s*" + valuePattern)
                .matcher(json);
        if (!matcher.find()) throw new AssertionError("JSON response does not contain " + name);
        return matcher.group(1);
    }

    private record PatientSession(String authorization) {
    }
}
