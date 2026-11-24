package com.medicalclinic.controller;

import com.medicalclinic.config.SecurityConfig;
import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.exception.PatientAlreadyExistsException;
import com.medicalclinic.exception.PatientNotFoundException;
import com.medicalclinic.mapper.PatientMapperImpl;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.service.PatientService;
import com.medicalclinic.service.VisitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PatientController.class)
@Import({PatientMapperImpl.class, SecurityConfig.class})
public class PatientControllerWebMvcTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    PatientService patientService;
    @MockitoBean
    VisitService visitService;

    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatientVisits_patientHasVisits_returns200WithVisitsInServiceOrder() throws Exception {
        // given
        LocalDateTime first = LocalDateTime.of(2030, 3, 4, 10, 0);
        LocalDateTime second = LocalDateTime.of(2030, 3, 5, 9, 0);
        when(visitService.findByPatientId(2L)).thenReturn(List.of(
                new VisitDto(8L, 1L, 2L, first, first.plusMinutes(30)),
                new VisitDto(3L, 4L, 2L, second, second.plusMinutes(15))));

        // when + then
        mockMvc.perform(get("/patients/2/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].patientId").value(2))
                .andExpect(jsonPath("$[1].id").value(3));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatientVisits_patientHasNoVisits_returns200WithEmptyList() throws Exception {
        // given
        when(visitService.findByPatientId(2L)).thenReturn(List.of());

        // when + then
        mockMvc.perform(get("/patients/2/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatientVisits_patientNotFound_returns404WithDetail() throws Exception {
        // given
        when(visitService.findByPatientId(99L)).thenThrow(new PatientNotFoundException(99L));

        // when + then
        mockMvc.perform(get("/patients/99/visits"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient with id: 99 not found"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatient_patientExists_returns200WithPatient() throws Exception {
        // given
        PatientDto dto = new PatientDto(5L,
                "jan@example.com",
                "Jan",
                "Kowalski",
                "123456789",
                null);
        when(patientService.findById(5L)).thenReturn(dto);

        // when + then
        mockMvc.perform(get("/patients/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("jan@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.idCardNo").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPatients_twoPatientsExist_returns200WithBoth() throws Exception {
        // given
        when(patientService.findAll()).thenReturn(List.of(
                new PatientDto(1L, "jan@example.com", "Jan", "Kowalski", "123456789", null),
                new PatientDto(2L, "anna@example.com", "Anna", "Nowak", "987654321", null)));

        // when + then
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Anna"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPatients_emailGiven_returns200WithSinglePatient() throws Exception {
        // given
        when(patientService.findByEmail("jan@example.com")).thenReturn(
                new PatientDto(1L, "jan@example.com", "Jan", "Kowalski", "123456789", null));

        // when + then
        mockMvc.perform(get("/patients").param("email", "jan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("jan@example.com"));
        verify(patientService, never()).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPatients_fragmentGiven_returns200WithMatches() throws Exception {
        // given
        when(patientService.searchByFragment("kowal")).thenReturn(List.of(
                new PatientDto(1L, "jan@example.com", "Jan", "Kowalski", "123456789", null)));

        // when + then
        mockMvc.perform(get("/patients").param("fragment", "kowal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].lastName").value("Kowalski"));
        verify(patientService, never()).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPatients_emailAndFragmentGiven_returns400() throws Exception {
        // when + then
        mockMvc.perform(get("/patients")
                        .param("email", "jan@example.com")
                        .param("fragment", "kowal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Use either 'email' or 'fragment', not both"));
        verify(patientService, never()).findByEmail(any());
        verify(patientService, never()).searchByFragment(any());
        verify(patientService, never()).findAll();
    }

    @Test
    void postPatient_validBody_returns201() throws Exception {
        // given
        String body = """
                {
                  "email": "jan@example.com",
                  "password": "tajneHaslo1",
                  "idCardNo": "ABC123456",
                  "firstName": "Jan",
                  "lastName": "Kowalski",
                  "phoneNumber": "123456789",
                  "birthday": "1990-01-01"
                }
                """;
        when(patientService.create(any(PatientCreateCommand.class))).thenReturn(
                new PatientDto(5L, "jan@example.com", "Jan", "Kowalski", "123456789", LocalDate.of(1990, 1, 1)));

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }


    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatient_patientNotFound_returns404WithDetail() throws Exception {
        // given
        when(patientService.findById(99L)).thenThrow(new PatientNotFoundException(99L));

        // when + then
        mockMvc.perform(get("/patients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient with id: 99 not found"));
    }

    @Test
    void postPatient_blankFirstName_returns400WithFieldError() throws Exception {
        // given
        String body = VALID_BODY.replace("\"firstName\": \"Jan\"", "\"firstName\": \"\"");

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("firstName"))
                .andExpect(jsonPath("$.errors[0].message").value("first name is required"));
        verify(patientService, never()).create(any());
    }

    @Test
    void postPatient_emailTaken_returns409WithDetail() throws Exception {
        // given
        when(patientService.create(any(PatientCreateCommand.class))).thenThrow(PatientAlreadyExistsException.emailTaken());

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Patient with this email already exists"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void deletePatient_patientExists_returns204() throws Exception {
        // given

        // when + then
        mockMvc.perform(delete("/patients/5"))
                .andExpect(status().isNoContent());
        verify(patientService).deleteById(5L);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void deletePatient_patientNotFound_returns404() throws Exception {
        // given
        doThrow(new PatientNotFoundException(99L)).when(patientService).deleteById(99L);

        // when + then
        mockMvc.perform(delete("/patients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient with id: 99 not found"));
    }



    @Test
    void postPatient_malformedJson_returns400WithDetail() throws Exception {
        // given
        String body = "{ \"email\": \"jan@example.com\", ";

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("request body is not valid JSON"));
        verify(patientService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatient_idNotANumber_returns400WithDetail() throws Exception {
        // given

        // when + then
        mockMvc.perform(get("/patients/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Parameter id has invalid type"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatient_unexpectedError_returns500WithoutDetails() throws Exception {
        // given
        when(patientService.findById(5L)).thenThrow(new IllegalStateException("connection pool exhausted"));

        // when + then
        mockMvc.perform(get("/patients/5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Unknown error"))
                .andExpect(content().string(not(containsString("connection pool exhausted"))));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void deletePatients_noId_returns405WithDetail() throws Exception {
        // given

        // when + then
        mockMvc.perform(delete("/patients"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.detail").value("method DELETE is not supported here"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void postPatient_bodyAsPlainText_returns415WithoutErrorLog(CapturedOutput output) throws Exception {
        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(VALID_BODY))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.detail").exists());
        assertThat(output.getOut()).contains("Request rejected: 415 HttpMediaTypeNotSupportedException");
        assertThat(output.getOut()).doesNotContain("Unexpected error");
        assertThat(output.getOut()).doesNotContain("jan@example.com");
        verify(patientService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @ExtendWith(OutputCaptureExtension.class)
    void getPatient_acceptXml_returns406WithoutErrorLog(CapturedOutput output) throws Exception {
        // given
        when(patientService.findById(5L)).thenReturn(
                new PatientDto(5L, "jan@example.com", "Jan", "Kowalski", "123456789", null));

        // when + then
        mockMvc.perform(get("/patients/5").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
        assertThat(output.getOut()).contains("Request rejected: 406 HttpMediaTypeNotAcceptableException");
        assertThat(output.getOut()).doesNotContain("Unexpected error");
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @ExtendWith(OutputCaptureExtension.class)
    void getPatient_idNotANumber_logsNoRequestValue(CapturedOutput output) throws Exception {
        // when + then
        mockMvc.perform(get("/patients/abc-secret-value"))
                .andExpect(status().isBadRequest());
        assertThat(output.getOut()).contains("Request rejected: 400 MethodArgumentTypeMismatchException");
        assertThat(output.getOut()).doesNotContain("abc-secret-value");
    }

    @Test
    void postPatient_firstNameOver100Characters_returns400WithFieldError() throws Exception {
        // given
        String body = VALID_BODY.replace("\"firstName\": \"Jan\"", "\"firstName\": \"" + "J".repeat(101) + "\"");

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("firstName"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("first name must be at most 100 characters long"));
        verify(patientService, never()).create(any());
    }

    @Test
    void postPatient_passwordOver72Bytes_returns400WithFieldError() throws Exception {
        // given
        String body = VALID_BODY.replace("tajneHaslo1", "ż".repeat(40));

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message").value("password must be at most 72 bytes long"));
        verify(patientService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @ExtendWith(OutputCaptureExtension.class)
    void getPatients_emailNotFound_returns404AndLogsNoEmail(CapturedOutput output) throws Exception {
        // given
        when(patientService.findByEmail("brak@example.com"))
                .thenThrow(new PatientNotFoundException("brak@example.com"));

        // when + then
        mockMvc.perform(get("/patients").param("email", "brak@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("brak@example.com")));
        assertThat(output.getOut()).contains("Domain error: 404 PatientNotFoundException");
        assertThat(output.getOut()).doesNotContain("brak@example.com");
    }

    @Test
    void postPatient_birthdayMissing_returns400WithFieldError() throws Exception {
        // given
        String body = VALID_BODY.replace("\"birthday\": \"1990-01-01\"", "\"birthday\": null");

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("birthday"))
                .andExpect(jsonPath("$.errors[0].message").value("birthday is required"));
        verify(patientService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void putPatient_validBody_returns200WithPatient() throws Exception {
        // given
        when(patientService.update(any(), any())).thenReturn(
                new PatientDto(5L, "jan@example.com", "Jan", "Kowalski", "123456789", LocalDate.of(1990, 1, 1)));

        // when + then
        mockMvc.perform(put("/patients/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthday").value("1990-01-01"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void putPatient_birthdayNull_returns400WithFieldError() throws Exception {
        // given
        String body = VALID_UPDATE_BODY.replace("\"1990-01-01\"", "null");

        // when + then
        mockMvc.perform(put("/patients/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("birthday"))
                .andExpect(jsonPath("$.errors[0].message").value("birthday is required"));
        verify(patientService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void putPatient_birthdayAbsent_returns400WithFieldError() throws Exception {
        // given
        String body = """
                {
                  "firstName": "Jan",
                  "lastName": "Kowalski",
                  "phoneNumber": "123456789"
                }
                """;

        // when + then
        mockMvc.perform(put("/patients/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("birthday"));
        verify(patientService, never()).update(any(), any());
    }

    private static final String VALID_UPDATE_BODY = """
            {
              "firstName": "Jan",
              "lastName": "Kowalski",
              "phoneNumber": "123456789",
              "birthday": "1990-01-01"
            }
            """;

    private static final String VALID_BODY = """
            {
              "email": "jan@example.com",
              "password": "tajneHaslo1",
              "idCardNo": "ABC123456",
              "firstName": "Jan",
              "lastName": "Kowalski",
              "phoneNumber": "123456789",
              "birthday": "1990-01-01"
            }
            """;
}
