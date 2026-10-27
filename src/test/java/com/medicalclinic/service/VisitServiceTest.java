package com.medicalclinic.service;

import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.DoctorNotFoundException;
import com.medicalclinic.exception.InvalidVisitException;
import com.medicalclinic.exception.PatientNotFoundException;
import com.medicalclinic.exception.VisitAlreadyBookedException;
import com.medicalclinic.exception.VisitNotFoundException;
import com.medicalclinic.exception.VisitOverlapsException;
import com.medicalclinic.mapper.VisitMapper;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Patient;
import com.medicalclinic.model.Visit;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.PatientRepository;
import com.medicalclinic.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 15, 8, 0);

    @Mock
    private VisitRepository visitRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;

    private VisitService visitService;

    @BeforeEach
    void setUp() {
        VisitMapper visitMapper = Mappers.getMapper(VisitMapper.class);
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        visitService = new VisitService(visitRepository, doctorRepository, patientRepository, visitMapper, clock);
    }

    @Test
    void create_startsQuarterBeforeNow_throwsInvalidVisit() {
        // given
        VisitCreateCommand command = new VisitCreateCommand(1L, NOW.minusMinutes(15), NOW.plusMinutes(15));

        // when
        InvalidVisitException exception = assertThrows(InvalidVisitException.class,
                () -> visitService.create(command));

        // then
        assertEquals("Visit cannot start in the past", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_startNotOnQuarterHour_throwsInvalidVisit() {
        // given
        LocalDateTime start = NOW.plusHours(2).plusMinutes(10);
        VisitCreateCommand command = new VisitCreateCommand(1L, start, start.plusMinutes(30));

        // when
        InvalidVisitException exception = assertThrows(InvalidVisitException.class,
                () -> visitService.create(command));

        // then
        assertEquals("Visit must start on a full quarter hour (e.g. 10:00, 10:15)", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_lengthNotMultipleOfQuarter_throwsInvalidVisit() {
        // given
        LocalDateTime start = NOW.plusHours(2);
        VisitCreateCommand command = new VisitCreateCommand(1L, start, start.plusMinutes(20));

        // when
        InvalidVisitException exception = assertThrows(InvalidVisitException.class,
                () -> visitService.create(command));

        // then
        assertEquals("Visit length must be a positive multiple of 15 minutes", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_doctorNotFound_throwsDoctorNotFound() {
        // given
        LocalDateTime start = NOW.plusHours(2);
        VisitCreateCommand command = new VisitCreateCommand(99L, start, start.plusMinutes(30));
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class,
                () -> visitService.create(command));

        // then
        assertEquals("Doctor with id: 99 not found", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_overlapsOtherVisit_throwsVisitOverlaps() {
        // given
        LocalDateTime start = NOW.plusHours(2);
        VisitCreateCommand command = new VisitCreateCommand(1L, start, start.plusMinutes(30));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(Doctor.builder().id(1L).build()));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(1L, start.plusMinutes(30), start))
                .thenReturn(true);

        // when
        VisitOverlapsException exception = assertThrows(VisitOverlapsException.class,
                () -> visitService.create(command));

        // then
        assertEquals("Doctor already has a visit in this time slot", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_startsExactlyNow_savesVisitWithDoctor() {
        // given
        Doctor doctor = Doctor.builder().id(1L).build();
        VisitCreateCommand command = new VisitCreateCommand(1L, NOW, NOW.plusMinutes(30));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(1L, NOW.plusMinutes(30), NOW))
                .thenReturn(false);
        when(visitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);

        // when
        visitService.create(command);

        // then
        verify(visitRepository).save(captor.capture());
        Visit saved = captor.getValue();
        assertEquals(NOW, saved.getStartsAt());
        assertEquals(NOW.plusMinutes(30), saved.getEndsAt());
        assertSame(doctor, saved.getDoctor());
    }

    @Test
    void book_visitYesterday_throwsInvalidVisit() {
        // given
        Visit visit = Visit.builder().id(5L).startsAt(NOW.minusDays(1)).endsAt(NOW.minusDays(1).plusMinutes(30)).build();
        when(visitRepository.findById(5L)).thenReturn(Optional.of(visit));

        // when
        InvalidVisitException exception = assertThrows(InvalidVisitException.class,
                () -> visitService.book(5L, 1L));

        // then
        assertEquals("Visit in the past cannot be booked", exception.getMessage());
    }

    @Test
    void book_visitNotFound_throwsVisitNotFound() {
        // given
        when(visitRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        VisitNotFoundException exception = assertThrows(VisitNotFoundException.class,
                () -> visitService.book(99L, 1L));

        // then
        assertEquals("Visit with id: 99 not found", exception.getMessage());
    }

    @Test
    void book_patientNotFound_throwsPatientNotFound() {
        // given
        Visit visit = Visit.builder().id(5L).startsAt(NOW.plusDays(1)).endsAt(NOW.plusDays(1).plusMinutes(30)).build();
        when(visitRepository.findById(5L)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> visitService.book(5L, 99L));

        // then
        assertEquals("Patient with id: 99 not found", exception.getMessage());
    }

    @Test
    void book_visitAlreadyBooked_throwsAndKeepsPatient() {
        // given
        Patient owner = Patient.builder().id(1L).build();
        Visit visit = Visit.builder().id(5L).startsAt(NOW.plusDays(1)).endsAt(NOW.plusDays(1).plusMinutes(30))
                .patient(owner).build();
        when(visitRepository.findById(5L)).thenReturn(Optional.of(visit));

        // when
        VisitAlreadyBookedException exception = assertThrows(VisitAlreadyBookedException.class,
                () -> visitService.book(5L, 2L));

        // then
        assertEquals("Visit with id: 5 is already booked", exception.getMessage());
        assertSame(owner, visit.getPatient());
    }

    @Test
    void book_freeVisit_assignsPatient() {
        // given
        Patient patient = Patient.builder().id(2L).build();
        Visit visit = Visit.builder().id(5L).startsAt(NOW.plusDays(1)).endsAt(NOW.plusDays(1).plusMinutes(30)).build();
        when(visitRepository.findById(5L)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

        // when
        VisitDto result = visitService.book(5L, 2L);

        // then
        assertSame(patient, visit.getPatient());
        assertEquals(2L, result.patientId());
    }
}
