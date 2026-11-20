package com.mediqr.backend;

import com.mediqr.backend.controller.DocumentoController;
import com.mediqr.backend.service.DocumentoService;
import com.mediqr.backend.service.DocumentoStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediqrBackendApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoadsWithoutDocumentStorageAndHealthIsAvailable() throws Exception {
		assertFalse(applicationContext.containsBean("documentoController"));
		assertFalse(applicationContext.containsBean("documentoService"));
		assertFalse(applicationContext.containsBean("documentoStorageService"));
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));
	}

}
