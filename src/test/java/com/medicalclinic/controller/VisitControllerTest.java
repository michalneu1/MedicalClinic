package com.medicalclinic.controller;

import com.medicalclinic.config.SecurityConfig;
import com.medicalclinic.dto.BookingSummaryDto;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.PatientNotFoundException;
import com.medicalclinic.exception.VisitAlreadyBookedException;
import com.medicalclinic.facade.VisitBookingFacade;
import com.medicalclinic.mapper.VisitMapperImpl;
import com.medicalclinic.model.Specialization;
import com.medicalclinic.service.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitController.class)
@Import({VisitMapperImpl.class, SecurityConfig.class})
@WithMockUser(roles = "PATIENT")
class VisitControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    VisitBookingFacade visitBookingFacade;
    @MockitoBean
    VisitService visitService;

    @Test
    void bookVisit_freeSlot_returns200WithSummary() throws Exception {
        // given
        LocalDateTime startsAt = LocalDateTime.of(2030, 3, 4, 10, 0);
        BookingSummaryDto summary = new BookingSummaryDto(
                new VisitDto(1L, 1L, 2L, startsAt, startsAt.plusMinutes(30)),
                new PatientDto(2L, "anna@example.com", "Anna", "Nowak", "500300400", null),
                new DoctorDto(1L, 4L, "kasia@example.com", "Katarzyna", "Wróbel",
                        Specialization.CARDIOLOGIST, List.of()),
                1L);
        when(visitBookingFacade.book(1L, 2L)).thenReturn(summary);

        // when + then
        mockMvc.perform(patch("/visits/1/book/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visit.patientId").value(2))
                .andExpect(jsonPath("$.patient.firstName").value("Anna"))
                .andExpect(jsonPath("$.patient.lastName").value("Nowak"))
                .andExpect(jsonPath("$.doctor.lastName").value("Wróbel"))
                .andExpect(jsonPath("$.freeSlotsThatDay").value(1));
        verifyNoInteractions(visitService);
    }

    @Test
    void bookVisit_slotAlreadyBooked_returns409WithDetail() throws Exception {
        // given
        when(visitBookingFacade.book(1L, 3L)).thenThrow(new VisitAlreadyBookedException(1L));

        // when + then
        mockMvc.perform(patch("/visits/1/book/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Visit with id: 1 is already booked"));
    }

    @Test
    void bookVisit_patientNotFound_returns404WithDetail() throws Exception {
        // given
        when(visitBookingFacade.book(2L, 99L)).thenThrow(new PatientNotFoundException(99L));

        // when + then
        mockMvc.perform(patch("/visits/2/book/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient with id: 99 not found"));
    }
}
