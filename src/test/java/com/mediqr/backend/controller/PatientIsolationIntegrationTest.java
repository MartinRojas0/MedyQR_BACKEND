package com.mediqr.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediqr.backend.model.Paciente;
import com.mediqr.backend.model.QrEmergencia;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.PacienteRepository;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.repository.QrEmergenciaRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import java.util.UUID;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientIsolationIntegrationTest {

    private static final String TEST_JWT_SECRET = "bWVkaXFyLXRlc3Qta2V5LWF0LWxlYXN0LTMyLWJ5dGVzIQ==";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QrEmergenciaRepository qrEmergenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PersonalSaludRepository personalSaludRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void unauthenticatedRequestsReturn401AndPatientsCannotAccessStaffRoutes() throws Exception {
        mockMvc.perform(get("/api/pacientes/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/pacientes/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        PatientSession patient = registerAndLogin(
                "roles.patient@example.test", "roles_patient", "roles_patient");
        mockMvc.perform(get("/api/citas").header("Authorization", patient.authorization()))
                .andExpect(status().isForbidden());
    }

    @Test
    void existingPatientAccountCanLoginRecoverProfileAndAccessPatientModules() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "existing." + suffix + "@example.test";
        String password = "existing-account-test-password";

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setUsername("existing_" + suffix);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol("PACIENTE");
        usuario.setActivo(true);
        Usuario savedUsuario = usuarioRepository.save(usuario);

        Paciente paciente = new Paciente();
        paciente.setUsuarioId(savedUsuario.getId());
        paciente.setNombres("Paciente existente");
        paciente.setApellidos("Prueba");
        pacienteRepository.save(paciente);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Login(email, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUsuario.getId()))
                .andExpect(jsonPath("$.rol").value("PACIENTE"))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("token").asText();
        String authorization = "Bearer " + token;

        mockMvc.perform(get("/api/auth/me").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUsuario.getId()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.rol").value("PACIENTE"));
        mockMvc.perform(get("/api/pacientes/me").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Paciente existente"));
        mockMvc.perform(get("/api/citas/me").header("Authorization", authorization))
                .andExpect(status().isOk());
    }

    @Test
    void malformedAndExpiredTokensAreUnauthorizedForProfileEndpoints() throws Exception {
        String expiredToken = Jwts.builder()
                .subject("expired@example.test")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_JWT_SECRET)), Jwts.SIG.HS256)
                .compact();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/pacientes/me").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientsCanLoginByEmailOrUsernameAndOnlyPersistTheirOwnData() throws Exception {
        PatientSession first = registerAndLogin(
                "patient.one@example.test", "patient_one", "patient.one@example.test");
        PatientSession second = registerAndLogin(
                "patient.two@example.test", "patient_two", "patient_two");

        updateProfile(first, "Paciente Uno", "Perfil Uno");
        updateProfile(second, "Paciente Dos", "Perfil Dos");
        updateHistory(first, "A+", "Alergia privada uno");
        updateHistory(second, "O-", "Alergia privada dos");

        mockMvc.perform(get("/api/pacientes/me").header("Authorization", first.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Paciente Uno"))
                .andExpect(jsonPath("$.apellidos").value("Perfil Uno"));
        mockMvc.perform(get("/api/pacientes/me").header("Authorization", second.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Paciente Dos"))
                .andExpect(jsonPath("$.apellidos").value("Perfil Dos"));

        mockMvc.perform(get("/api/historial-clinico/me").header("Authorization", first.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoSangre").value("A+"))
                .andExpect(jsonPath("$.alergias").value("Alergia privada uno"));
        mockMvc.perform(get("/api/historial-clinico/me").header("Authorization", second.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoSangre").value("O-"))
                .andExpect(jsonPath("$.alergias").value("Alergia privada dos"));

        mockMvc.perform(get("/api/pacientes/{id}", second.patientId())
                        .header("Authorization", first.authorization()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/pacientes/{id}", second.patientId())
                        .header("Authorization", first.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson("No autorizado", "No autorizado")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/pacientes/me").header("Authorization", second.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Paciente Dos"));

        QrEmergencia secondQr = new QrEmergencia();
        secondQr.setPacienteId(second.patientId());
        secondQr.setToken(UUID.randomUUID());
        QrEmergencia savedQr = qrEmergenciaRepository.save(secondQr);

        mockMvc.perform(get("/api/qr-emergencia/{id}", savedQr.getId())
                        .header("Authorization", first.authorization()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/qr-emergencia/paciente/{id}", second.patientId())
                        .header("Authorization", first.authorization()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/qr-emergencia/{id}", savedQr.getId())
                        .header("Authorization", first.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/qr-emergencia/{id}", savedQr.getId())
                        .header("Authorization", first.authorization()))
                .andExpect(status().isForbidden());
    }

    @Test
    void emergencyContactIsPrivateAndPersistsOnlyForItsPatient() throws Exception {
        PatientSession first = registerAndLogin(
                "contact.one@example.test", "contact_one", "contact_one");
        PatientSession second = registerAndLogin(
                "contact.two@example.test", "contact_two", "contact_two");

        mockMvc.perform(put("/api/pacientes/me/contacto-emergencia")
                        .header("Authorization", first.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Contacto Uno\",\"parentesco\":\"Hermana\",\"telefono\":\"+51 900 000 001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Contacto Uno"))
                .andExpect(jsonPath("$.telefono").value("+51 900 000 001"));

        mockMvc.perform(get("/api/pacientes/me/contacto-emergencia")
                        .header("Authorization", first.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Contacto Uno"));
        mockMvc.perform(get("/api/pacientes/me/contacto-emergencia")
                        .header("Authorization", second.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nullValue()));
        mockMvc.perform(get("/api/pacientes/me").header("Authorization", first.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactoEmergenciaNombre").doesNotExist())
                .andExpect(jsonPath("$.contactoEmergenciaTelefono").doesNotExist());

        mockMvc.perform(put("/api/pacientes/me/contacto-emergencia")
                        .header("Authorization", second.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Contacto Dos\",\"parentesco\":\"Amigo\",\"telefono\":\"+51 900 000 002\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/pacientes/me/contacto-emergencia")
                        .header("Authorization", first.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Contacto Uno"));
    }

    @Test
    void secureQrIsHashedOwnerScopedLimitedExpiredAndRevocable() throws Exception {
        PatientSession owner = registerAndLogin(
                "qr.owner@example.test", "qr_owner", "qr_owner");
        PatientSession other = registerAndLogin(
                "qr.other@example.test", "qr_other", "qr_other");
        updateProfile(owner, "Paciente QR", "Propietario");
        updateHistory(owner, "AB+", "Alergia QR privada");

        JsonNode issued = issueQr(owner, true, true, false, false);
        String token = issued.path("token").asText();
        assertEquals(43, token.length());
        assertEquals(true, issued.path("publicUrl").asText().contains("/#/ficha/" + token));

        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(java.util.Base64.getUrlDecoder().decode(token)));
        QrEmergencia persistedQr = qrEmergenciaRepository.findByTokenHash(hash).orElseThrow();
        assertEquals(null, persistedQr.getToken());
        assertEquals(owner.patientId(), persistedQr.getPacienteId());
        assertEquals(true, persistedQr.getExpiresAt().isAfter(OffsetDateTime.now()));

        mockMvc.perform(post("/api/qr-emergencia/public/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublicQrToken(token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").doesNotExist())
                .andExpect(jsonPath("$.apellidos").doesNotExist())
                .andExpect(jsonPath("$.tipoSangre").value("AB+"))
                .andExpect(jsonPath("$.alergias").value("Alergia QR privada"))
                .andExpect(jsonPath("$.pacienteId").doesNotExist())
                .andExpect(jsonPath("$.documentoIdentidad").doesNotExist())
                .andExpect(jsonPath("$.fechaNacimiento").doesNotExist())
                .andExpect(jsonPath("$.telefono").doesNotExist())
                .andExpect(jsonPath("$.medicamentos").doesNotExist());

        MvcResult image = mockMvc.perform(post("/api/qr-emergencia/me/image")
                        .header("Authorization", owner.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublicQrToken(token))))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andReturn();
        byte[] png = image.getResponse().getContentAsByteArray();
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 'P', png[1]);

        mockMvc.perform(post("/api/qr-emergencia/me/image")
                        .header("Authorization", other.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublicQrToken(token))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/qr-emergencia/me")
                        .header("Authorization", owner.authorization()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/qr-emergencia/public/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublicQrToken(token))))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.state").value("revoked"));
    }

    @Test
    void secureQrReportsNoInformationExpiredAndInvalidStates() throws Exception {
        PatientSession owner = registerAndLogin(
                "qr.states@example.test", "qr_states", "qr_states");
        JsonNode issued = issueQr(owner, false, false, false, false);
        String token = issued.path("token").asText();

        mockMvc.perform(post("/api/qr-emergencia/public/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublicQrToken(token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.informacionMedicaDisponible").value(false));

        mockMvc.perform(post("/api/qr-emergencia/public/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublicQrToken("invalid"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.state").value("invalid"));

        QrEmergencia expired = qrEmergenciaRepository.findByPacienteId(owner.patientId()).stream()
                .filter(qr -> qr.getTokenHash() != null)
                .findFirst().orElseThrow();
        expired.setExpiresAt(OffsetDateTime.now().minusSeconds(1));
        qrEmergenciaRepository.save(expired);
        mockMvc.perform(post("/api/qr-emergencia/public/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublicQrToken(token))))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.state").value("expired"));
    }

    @Test
    void missingAppointmentReturnsNotFoundInsteadOfAuthorizationFailure() throws Exception {
        ProfessionalSession professional = registerProfessional(
                "missing.appointment@example.test", "missing_appointment");

        mockMvc.perform(get("/api/citas/999999")
                        .header("Authorization", professional.authorization()))
                .andExpect(status().isNotFound());
    }

    @Test
    void professionalPatientListAndMedicalBoardRequirePatientAuthorization() throws Exception {
        PatientSession owner = registerAndLogin(
                "owner.authorization@example.test", "owner_auth", "owner_auth");
        PatientSession otherPatient = registerAndLogin(
                "other.authorization@example.test", "other_auth", "other_auth");
        updateProfile(owner, "Paciente Autorizado", "Dueño");
        updateProfile(otherPatient, "Paciente No Autorizado", "Otra Cuenta");

        ProfessionalSession authorizedProfessional = registerProfessional(
                "authorized.doctor@example.test", "authorized_doctor");
        ProfessionalSession secondProfessional = registerProfessional(
                "second.doctor@example.test", "second_doctor");

        mockMvc.perform(get("/api/pacientes")
                        .header("Authorization", authorizedProfessional.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/pacientes")
                        .header("Authorization", owner.authorization()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/pacientes/{id}", owner.patientId())
                        .header("Authorization", authorizedProfessional.authorization()))
                .andExpect(status().isForbidden());

        authorize(owner, authorizedProfessional);
        mockMvc.perform(get("/api/pacientes")
                        .header("Authorization", authorizedProfessional.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(owner.patientId()))
                .andExpect(jsonPath("$[0].nombres").value("Paciente Autorizado"))
                .andExpect(jsonPath("$[0].documentoIdentidad").doesNotExist())
                .andExpect(jsonPath("$[0].fechaNacimiento").doesNotExist())
                .andExpect(jsonPath("$[0].telefono").doesNotExist());
        mockMvc.perform(get("/api/pacientes/{id}", otherPatient.patientId())
                        .header("Authorization", authorizedProfessional.authorization()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/autorizaciones-paciente/paciente/{id}", owner.patientId())
                        .header("Authorization", otherPatient.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Authorization(authorizedProfessional.personalId()))))
                .andExpect(status().isForbidden());

        String juntaJson = objectMapper.writeValueAsString(new JuntaRequest(
                owner.patientId(), "Evaluación interdisciplinaria",
                OffsetDateTime.now().minusMinutes(1).toString(),
                OffsetDateTime.now().plusDays(1).toString()));
        mockMvc.perform(post("/api/juntas-medicas")
                        .header("Authorization", secondProfessional.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(juntaJson))
                .andExpect(status().isForbidden());

        MvcResult juntaResult = mockMvc.perform(post("/api/juntas-medicas")
                        .header("Authorization", authorizedProfessional.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(juntaJson))
                .andExpect(status().isCreated())
                .andReturn();
        Long juntaId = objectMapper.readTree(juntaResult.getResponse().getContentAsString())
                .path("id").asLong();

        String participantJson = objectMapper.writeValueAsString(new Authorization(secondProfessional.personalId()));
        mockMvc.perform(post("/api/participantes-junta/junta/{id}", juntaId)
                        .header("Authorization", authorizedProfessional.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(participantJson))
                .andExpect(status().isForbidden());

        authorize(owner, secondProfessional);
        mockMvc.perform(post("/api/participantes-junta/junta/{id}", juntaId)
                        .header("Authorization", authorizedProfessional.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(participantJson))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/pacientes/{id}", owner.patientId())
                        .header("Authorization", secondProfessional.authorization()))
                .andExpect(status().isOk());

        Long secondAuthorizationId = findAuthorizationId(owner, secondProfessional.personalId());
        mockMvc.perform(put("/api/autorizaciones-paciente/{id}/revocar", secondAuthorizationId)
                        .header("Authorization", owner.authorization()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/pacientes/{id}", owner.patientId())
                        .header("Authorization", secondProfessional.authorization()))
                .andExpect(status().isForbidden());
    }

    private ProfessionalSession registerProfessional(String email, String username) throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Registration(email, username, "test-password"))))
                .andExpect(status().isCreated());

        Usuario user = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        pacienteRepository.findByUsuarioId(user.getId()).ifPresent(pacienteRepository::delete);
        user.setRol("PERSONAL_SALUD");
        usuarioRepository.save(user);

        PersonalSalud personal = new PersonalSalud();
        personal.setUsuarioId(user.getId());
        personal.setNombres("Profesional");
        personal.setApellidos(username);
        personal.setActivo(true);
        personal = personalSaludRepository.save(personal);

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Login(username, "test-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("PERSONAL_SALUD"))
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();
        return new ProfessionalSession(token, personal.getId());
    }

    private void authorize(PatientSession patient, ProfessionalSession professional) throws Exception {
        mockMvc.perform(post("/api/autorizaciones-paciente/paciente/{id}", patient.patientId())
                        .header("Authorization", patient.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Authorization(professional.personalId()))))
                .andExpect(status().isCreated());
    }

    private Long findAuthorizationId(PatientSession patient, Long personalId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/autorizaciones-paciente/paciente/{id}", patient.patientId())
                        .header("Authorization", patient.authorization()))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode authorization : objectMapper.readTree(result.getResponse().getContentAsString())) {
            if (authorization.path("personalId").asLong() == personalId) {
                return authorization.path("id").asLong();
            }
        }
        throw new AssertionError("Patient authorization was not found");
    }

    private JsonNode issueQr(PatientSession patient, boolean blood, boolean allergies,
                             boolean conditions, boolean medicines) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/qr-emergencia/me")
                        .header("Authorization", patient.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new QrOptions(blood, allergies, medicines, conditions))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private PatientSession registerAndLogin(String email, String username, String identifier) throws Exception {
        String registration = objectMapper.writeValueAsString(new Registration(email, username, "test-password"));
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isCreated());

        String login = objectMapper.writeValueAsString(new Login(identifier, "test-password"));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(email, response.path("email").asText());
        String token = response.path("token").asText();
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.rol").value("PACIENTE"));
        MvcResult patientResult = mockMvc.perform(get("/api/pacientes/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        Long patientId = objectMapper.readTree(patientResult.getResponse().getContentAsString())
                .path("id").asLong();
        return new PatientSession(token, patientId);
    }

    private void updateProfile(PatientSession patient, String firstName, String lastName) throws Exception {
        mockMvc.perform(put("/api/pacientes/me")
                        .header("Authorization", patient.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson(firstName, lastName)))
                .andExpect(status().isOk());
    }

    private String profileJson(String firstName, String lastName) throws Exception {
        return objectMapper.writeValueAsString(new Profile(firstName, lastName));
    }

    private void updateHistory(PatientSession patient, String bloodType, String allergy) throws Exception {
        String history = objectMapper.writeValueAsString(new History(bloodType, allergy));
        mockMvc.perform(put("/api/historial-clinico/me")
                        .header("Authorization", patient.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(history))
                .andExpect(status().isOk());
    }

    private record Registration(String email, String username, String password) {
    }

    private record Login(String email, String password) {
    }

    private record Profile(String nombres, String apellidos) {
    }

    private record History(String tipoSangre, String alergias) {
    }

    private record QrOptions(Boolean mostrarTipoSangre, Boolean mostrarAlergias,
                             Boolean mostrarMedicamentos, Boolean mostrarEnfermedades) {
    }

    private record PublicQrToken(String token) {
    }

    private record Authorization(Long personalId) {
    }

    private record JuntaRequest(Long pacienteId, String motivo,
                                String fechaInicio, String fechaExpiracion) {
    }

    private record PatientSession(String token, Long patientId) {
        String authorization() {
            return "Bearer " + token;
        }
    }

    private record ProfessionalSession(String token, Long personalId) {
        String authorization() {
            return "Bearer " + token;
        }
    }
}
