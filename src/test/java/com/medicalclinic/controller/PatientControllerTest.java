package com.medicalclinic.controller;

import com.jayway.jsonpath.JsonPath;
import com.medicalclinic.model.Patient;
import com.medicalclinic.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    private static final String PASSWORD = "tajneHaslo1";
    private static final String UPDATE_BODY = """
            {
              "firstName": "Ewelina",
              "lastName": "Nowa",
              "phoneNumber": "111222333",
              "birthday": "1986-06-06"
            }
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PatientRepository patientRepository;

    @Test
    void postPatient_validBody_returns201WithPatient() throws Exception {
        // given
        String email = newEmail();

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(email, newIdCardNo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").value("Ewa"))
                .andExpect(jsonPath("$.lastName").value("Zielinska"))
                .andExpect(jsonPath("$.phoneNumber").value("555666777"))
                .andExpect(jsonPath("$.birthday").value("1985-05-05"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.idCardNo").doesNotExist());
    }

    @Test
    void postPatient_emailTaken_returns409WithDetail() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(patient.email(), newIdCardNo())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Patient with this email already exists"));
    }

    @Test
    void postPatient_idCardNoOfAnotherPatient_returns409WithDetail() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(newEmail(), patient.idCardNo())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Patient with this id card number already exists"));
    }

    @Test
    void postPatient_blankFirstName_returns400WithFieldError() throws Exception {
        // given
        String body = createBody(newEmail(), newIdCardNo()).replace("\"Ewa\"", "\"\"");

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("firstName"))
                .andExpect(jsonPath("$.errors[0].message").value("first name is required"));
    }

    @Test
    void getPatient_patientExists_returns200WithPatient() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(get("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.id()))
                .andExpect(jsonPath("$.email").value(patient.email()))
                .andExpect(jsonPath("$.birthday").value("1985-05-05"));
    }

    @Test
    void getPatient_patientNotFound_returns404WithDetail() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(get("/patients/999999")
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient with id: 999999 not found"));
    }

    @Test
    void getPatient_noToken_returns401() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(get("/patients/{id}", patient.id()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPatients_admin_returns200WithRegisteredPatient() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(get("/patients")
                        .header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(hasItem(patient.email())));
    }

    @Test
    void getPatients_patientRole_returns403() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(get("/patients")
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPatients_emailGiven_returns200WithSinglePatient() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(get("/patients").param("email", patient.email())
                        .header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.id()))
                .andExpect(jsonPath("$.firstName").value("Ewa"));
    }

    @Test
    void getPatients_emailNotFound_returns404WithDetail() throws Exception {
        // when + then
        mockMvc.perform(get("/patients").param("email", "nikt@example.com")
                        .header("Authorization", adminBearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient with email: nikt@example.com not found"));
    }

    @Test
    void getPatients_fragmentGiven_returns200WithMatches() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(get("/patients").param("fragment", "ielins")
                        .header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(patient.id())));
    }

    @Test
    void getPatients_emailAndFragmentGiven_returns400WithDetail() throws Exception {
        // when + then
        mockMvc.perform(get("/patients").param("email", "a@example.com").param("fragment", "a")
                        .header("Authorization", adminBearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Use either 'email' or 'fragment', not both"));
    }

    @Test
    void getPatientVisits_patientHasNoVisits_returns200WithEmptyList() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(get("/patients/{id}/visits", patient.id())
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void putPatient_validBody_returns200AndChangesOnlyEditableData() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when
        mockMvc.perform(put("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ewelina"));

        // then
        mockMvc.perform(get("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(patient.email()))
                .andExpect(jsonPath("$.firstName").value("Ewelina"))
                .andExpect(jsonPath("$.lastName").value("Nowa"))
                .andExpect(jsonPath("$.phoneNumber").value("111222333"))
                .andExpect(jsonPath("$.birthday").value("1986-06-06"));
    }

    @Test
    void putPatient_idCardNoInBody_keepsIdCardNo() throws Exception {
        // given
        RegisteredPatient patient = register();
        String body = UPDATE_BODY.replace("{", "{ \"idCardNo\": \"ZZZ999999\",");

        // when
        mockMvc.perform(put("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // then
        assertEquals(patient.idCardNo(), storedPatient(patient).getIdCardNo());
    }

    @Test
    void putPatient_blankFirstName_returns400AndKeepsData() throws Exception {
        // given
        RegisteredPatient patient = register();
        String body = UPDATE_BODY.replace("\"Ewelina\"", "\"\"");

        // when
        mockMvc.perform(put("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("firstName"))
                .andExpect(jsonPath("$.errors[0].message").value("first name is required"));

        // then
        assertEquals("Ewa", storedPatient(patient).getFirstName());
    }

    @Test
    void putPatient_birthdayAbsent_returns400AndKeepsBirthday() throws Exception {
        // given
        RegisteredPatient patient = register();
        String body = """
                {
                  "firstName": "Ewelina",
                  "lastName": "Nowa",
                  "phoneNumber": "111222333"
                }
                """;

        // when
        mockMvc.perform(put("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("birthday"))
                .andExpect(jsonPath("$.errors[0].message").value("birthday is required"));

        // then
        assertEquals(LocalDate.of(1985, 5, 5), storedPatient(patient).getBirthday());
    }

    @Test
    void putPatient_patientNotFound_returns404WithDetail() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(put("/patients/999999")
                        .header("Authorization", bearer(patient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient with id: 999999 not found"));
    }

    @Test
    void patchPassword_validPassword_newPasswordLogsInAndOldDoesNot() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when
        mockMvc.perform(patch("/patients/{id}/password", patient.id())
                        .header("Authorization", bearer(patient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "noweHaslo123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(patient.email()))
                .andExpect(jsonPath("$.password").doesNotExist());

        // then
        login(patient.email(), "noweHaslo123");
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(patient.email(), PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchPassword_tooShort_returns400WithFieldError() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(patch("/patients/{id}/password", patient.id())
                        .header("Authorization", bearer(patient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "krotkie"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message").value("password must be at least 8 characters long"));
    }

    @Test
    void deletePatient_patientWithoutVisits_returns204AndFreesEmail() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when
        mockMvc.perform(delete("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(patient.email(), PASSWORD)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(patient.email(), patient.idCardNo())))
                .andExpect(status().isCreated());
    }

    @Test
    void deletePatient_patientHasBookedVisit_returns409WithDetail() throws Exception {
        // given
        RegisteredPatient patient = register();
        int visitId = createVisitOfNewDoctor();
        mockMvc.perform(patch("/visits/{visitId}/book/{patientId}", visitId, patient.id())
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isOk());

        // when + then
        mockMvc.perform(delete("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Patient with id: " + patient.id() + " has visits and cannot be deleted"));
        mockMvc.perform(get("/patients/{id}", patient.id())
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isOk());
    }

    @Test
    void deletePatient_patientNotFound_returns404WithDetail() throws Exception {
        // given
        RegisteredPatient patient = register();

        // when + then
        mockMvc.perform(delete("/patients/999999")
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient with id: 999999 not found"));
    }

    private record RegisteredPatient(int id, String email, String idCardNo, String token) {
    }

    private RegisteredPatient register() throws Exception {
        String email = newEmail();
        String idCardNo = newIdCardNo();
        String response = mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(email, idCardNo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(response, "$.id");
        return new RegisteredPatient(id, email, idCardNo, login(email, PASSWORD));
    }

    private int createVisitOfNewDoctor() throws Exception {
        String adminToken = adminBearer();
        String user = mockMvc.perform(post("/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s", "role": "DOCTOR"}
                                """.formatted(newEmail(), PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int userId = JsonPath.read(user, "$.id");
        String doctor = mockMvc.perform(post("/doctors")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": %d, "firstName": "Adam", "lastName": "Lekarz",
                                 "specialization": "CARDIOLOGIST"}
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int doctorId = JsonPath.read(doctor, "$.id");
        LocalDate day = LocalDate.now().plusYears(1);
        String visit = mockMvc.perform(post("/visits")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"doctorId": %d, "startsAt": "%sT10:00:00", "endsAt": "%sT10:30:00"}
                                """.formatted(doctorId, day, day)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(visit, "$.id");
    }

    private Patient storedPatient(RegisteredPatient patient) {
        return patientRepository.findById((long) patient.id()).orElseThrow();
    }

    private String bearer(RegisteredPatient patient) {
        return "Bearer " + patient.token();
    }

    private String adminBearer() throws Exception {
        return "Bearer " + login("admin", "admin");
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String loginBody(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    private String createBody(String email, String idCardNo) {
        return """
                {
                  "email": "%s",
                  "password": "%s",
                  "idCardNo": "%s",
                  "firstName": "Ewa",
                  "lastName": "Zielinska",
                  "phoneNumber": "555666777",
                  "birthday": "1985-05-05"
                }
                """.formatted(email, PASSWORD, idCardNo);
    }

    private String newEmail() {
        return UUID.randomUUID() + "@example.com";
    }

    private String newIdCardNo() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            letters.append((char) ('A' + random.nextInt(26)));
        }
        return letters + "%06d".formatted(random.nextInt(1_000_000));
    }
}
