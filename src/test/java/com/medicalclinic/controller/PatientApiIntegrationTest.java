package com.medicalclinic.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PatientApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postThenGetByEmail_newPatient_isFound() throws Exception {
        // given
        String body = """
                {
                  "email": "nowy.pacjent@example.com",
                  "password": "tajneHaslo1",
                  "idCardNo": "XYZ987654",
                  "firstName": "Ewa",
                  "lastName": "Zielinska",
                  "phoneNumber": "555666777",
                  "birthday": "1985-05-05"
                }
                """;
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // when + then
        String patientToken = login("nowy.pacjent@example.com", "tajneHaslo1");
        mockMvc.perform(get("/patients").param("email", "nowy.pacjent@example.com")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        String adminToken = login("admin", "admin");
        mockMvc.perform(get("/patients").param("email", "nowy.pacjent@example.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nowy.pacjent@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ewa"));
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }
}
