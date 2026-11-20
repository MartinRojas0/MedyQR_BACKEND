package com.mediqr.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.model.Usuario;
import com.mediqr.backend.repository.PersonalSaludRepository;
import com.mediqr.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MiCitaControllerIntegrationTest {

    private static final String PASSWORD = "appointment-test-password";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonalSaludRepository personalSaludRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void patientsCanManageOnlyTheirOwnAppointmentsAcrossSessions() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        PatientSession pedro = registerAndLogin("pedro." + suffix + "@example.test", "pedro_" + suffix);
        PatientSession martin = registerAndLogin("martin." + suffix + "@example.test", "martin_" + suffix);
        PatientSession veronica = registerAndLogin("veronica." + suffix + "@example.test", "veronica_" + suffix);
        Professional professional = createProfessional(suffix);

        mockMvc.perform(get("/api/citas/me/opciones-personal")
                        .header("Authorization", pedro.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(professional.id()))
                .andExpect(jsonPath("$[0].nombre").value("Dra. Demo Profesional"))
                .andExpect(jsonPath("$[0].email").doesNotExist());

        OffsetDateTime firstTime = futureTime(40);
        String createRequest = appointmentBody(professional.id(), firstTime, "Control de rutina");
        MvcResult created = mockMvc.perform(post("/api/citas/me")
                        .header("Authorization", pedro.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.motivo").value("Control de rutina"))
                .andExpect(jsonPath("$.pacienteId").doesNotExist())
                .andReturn();
        long appointmentId = longField(created, "id");

        mockMvc.perform(get("/api/citas/me")
                        .header("Authorization", pedro.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(appointmentId));
        mockMvc.perform(get("/api/citas/me/{id}", appointmentId)
                        .header("Authorization", pedro.authorization()))
                .andExpect(status().isOk());

        for (PatientSession other : new PatientSession[]{martin, veronica}) {
            mockMvc.perform(get("/api/citas/me").header("Authorization", other.authorization()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
            mockMvc.perform(get("/api/citas/me/{id}", appointmentId)
                            .header("Authorization", other.authorization()))
                    .andExpect(status().isNotFound());
            mockMvc.perform(put("/api/citas/me/{id}", appointmentId)
                            .header("Authorization", other.authorization())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fechaHora\":\"" + futureTime(41) + "\"}"))
                    .andExpect(status().isNotFound());
            mockMvc.perform(patch("/api/citas/me/{id}/cancelar", appointmentId)
                            .header("Authorization", other.authorization()))
                    .andExpect(status().isNotFound());
        }

        mockMvc.perform(post("/api/citas/me")
                        .header("Authorization", martin.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentBody(professional.id(), firstTime, null)))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/citas/me/disponibilidad")
                        .header("Authorization", veronica.authorization())
                        .queryParam("personalId", String.valueOf(professional.id()))
                        .queryParam("fechaHora", firstTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));

        mockMvc.perform(post("/api/citas/me")
                        .header("Authorization", pedro.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentBody(
                                professional.id(), OffsetDateTime.now().minusDays(1), "Fecha inválida")))
                .andExpect(status().isBadRequest());

        OffsetDateTime newTime = futureTime(42);
        MvcResult rescheduled = mockMvc.perform(put("/api/citas/me/{id}", appointmentId)
                        .header("Authorization", pedro.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaHora\":\"" + newTime + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(OffsetDateTime.parse(stringField(rescheduled, "fechaHora")).isEqual(newTime));

        mockMvc.perform(patch("/api/citas/me/{id}/cancelar", appointmentId)
                        .header("Authorization", pedro.authorization()))
                .andExpect(status().isNoContent());
        PatientSession pedroAfterRelogin = login(pedro.identifier());
        mockMvc.perform(get("/api/citas/me/{id}", appointmentId)
                        .header("Authorization", pedroAfterRelogin.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(get("/api/citas/me/disponibilidad")
                        .header("Authorization", veronica.authorization())
                        .queryParam("personalId", String.valueOf(professional.id()))
                        .queryParam("fechaHora", newTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));

        mockMvc.perform(get("/api/citas").header("Authorization", pedro.authorization()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/citas/me").header("Authorization", professional.authorization()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/citas/{id}", appointmentId)
                        .header("Authorization", professional.authorization()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/citas/{id}", appointmentId)
                        .header("Authorization", professional.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"personalId\":" + professional.id() + ",\"fechaHora\":\"" + futureTime(43) + "\"}"))
                .andExpect(status().isForbidden());
    }

    private PatientSession registerAndLogin(String email, String username) throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"email\":\"%s\",\"username\":\"%s\",\"password\":\"%s\"}",
                                email, username, PASSWORD)))
                .andExpect(status().isCreated());
        return login(username);
    }

    private PatientSession login(String identifier) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"identifier\":\"%s\",\"password\":\"%s\"}", identifier, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return new PatientSession(identifier, "Bearer " + stringField(result, "token"));
    }

    private Professional createProfessional(String suffix) {
        Usuario user = new Usuario();
        user.setEmail("professional." + suffix + "@example.test");
        user.setUsername("professional_" + suffix);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRol("PERSONAL_SALUD");
        user.setActivo(true);
        Usuario savedUser = usuarioRepository.save(user);

        PersonalSalud professional = new PersonalSalud();
        professional.setUsuarioId(savedUser.getId());
        professional.setNombres("Dra. Demo");
        professional.setApellidos("Profesional");
        professional.setEspecialidad("Medicina General");
        professional.setActivo(true);
        PersonalSalud savedProfessional = personalSaludRepository.save(professional);
        return new Professional(savedProfessional.getId(), loginUnchecked("professional_" + suffix));
    }

    private String loginUnchecked(String identifier) {
        try {
            return login(identifier).authorization();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not authenticate test professional", exception);
        }
    }

    private String appointmentBody(Long personalId, OffsetDateTime fechaHora, String motivo) throws Exception {
        var body = objectMapper.createObjectNode();
        body.put("personalId", personalId);
        body.put("fechaHora", fechaHora.toString());
        if (motivo != null) body.put("motivo", motivo);
        return objectMapper.writeValueAsString(body);
    }

    private OffsetDateTime futureTime(int days) {
        return OffsetDateTime.now().plusDays(days).withHour(14).withMinute(0).withSecond(0).withNano(0);
    }

    private long longField(MvcResult result, String name) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path(name).asLong();
    }

    private String stringField(MvcResult result, String name) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path(name).asText();
    }

    private record PatientSession(String identifier, String authorization) {
    }

    private record Professional(Long id, String authorization) {
    }
}
