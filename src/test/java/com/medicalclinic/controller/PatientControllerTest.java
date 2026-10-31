package com.medicalclinic.controller;

import com.medicalclinic.config.SecurityConfig;
import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.exception.PatientAlreadyExistsException;
import com.medicalclinic.exception.PatientNotFoundException;
import com.medicalclinic.mapper.PatientMapperImpl;
import com.medicalclinic.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PatientController.class)
@Import({PatientMapperImpl.class, SecurityConfig.class})
public class PatientControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    PatientService patientService;

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
        when(patientService.create(any(PatientCreateCommand.class))).thenThrow(new PatientAlreadyExistsException());

        // when + then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Patient already exists"));
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
