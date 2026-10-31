package com.medicalclinic.facade;

import com.medicalclinic.dto.BookingSummaryDto;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.VisitAlreadyBookedException;
import com.medicalclinic.model.Specialization;
import com.medicalclinic.service.DoctorService;
import com.medicalclinic.service.PatientService;
import com.medicalclinic.service.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitBookingFacadeTest {
    @Mock
    VisitService visitService;
    @Mock
    PatientService patientService;
    @Mock
    DoctorService doctorService;

    VisitBookingFacade facade;

    @BeforeEach
    void setUp() {
        facade = new VisitBookingFacade(visitService, patientService, doctorService);
    }

    @Test
    void book_freeSlot_returnsSummaryWithFreeSlotsCountedAfterBooking() {
        // given
        LocalDateTime startsAt = LocalDateTime.of(2030, 3, 4, 10, 0);
        VisitDto visit = new VisitDto(1L, 7L, 2L, startsAt, startsAt.plusMinutes(30));
        PatientDto patient = new PatientDto(2L, "anna@example.com", "Anna", "Nowak", "500300400", null);
        DoctorDto doctor = new DoctorDto(7L, 4L, "kasia@example.com", "Katarzyna", "Wróbel",
                Specialization.CARDIOLOGIST, List.of());
        when(visitService.book(1L, 2L)).thenReturn(visit);
        when(patientService.findById(2L)).thenReturn(patient);
        when(doctorService.findById(7L)).thenReturn(doctor);
        when(visitService.countFreeSlots(7L, LocalDate.of(2030, 3, 4))).thenReturn(1L);

        // when
        BookingSummaryDto summary = facade.book(1L, 2L);

        // then
        assertSame(visit, summary.visit());
        assertSame(patient, summary.patient());
        assertSame(doctor, summary.doctor());
        assertEquals(1L, summary.freeSlotsThatDay());
        InOrder inOrder = inOrder(visitService);
        inOrder.verify(visitService).book(1L, 2L);
        inOrder.verify(visitService).countFreeSlots(7L, LocalDate.of(2030, 3, 4));
        verify(patientService).findById(2L);
        verify(doctorService).findById(7L);
    }

    @Test
    void book_slotAlreadyBooked_propagatesExceptionWithoutReadingPatientOrDoctor() {
        // given
        when(visitService.book(1L, 2L)).thenThrow(new VisitAlreadyBookedException(1L));

        // when
        VisitAlreadyBookedException exception = assertThrows(VisitAlreadyBookedException.class,
                () -> facade.book(1L, 2L));

        // then
        assertEquals("Visit with id: 1 is already booked", exception.getMessage());
        verifyNoInteractions(patientService, doctorService);
    }
}
