package com.medicalclinic.service;

import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.DoctorNotFoundException;
import com.medicalclinic.exception.InvalidSortPropertyException;
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
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
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
        verify(visitRepository, never()).saveAndFlush(any());
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
        verify(visitRepository, never()).saveAndFlush(any());
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
        verify(visitRepository, never()).saveAndFlush(any());
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
        verify(visitRepository, never()).saveAndFlush(any());
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
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_startsExactlyNow_savesVisitWithDoctor() {
        // given
        Doctor doctor = Doctor.builder().id(1L).build();
        VisitCreateCommand command = new VisitCreateCommand(1L, NOW, NOW.plusMinutes(30));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(1L, NOW.plusMinutes(30), NOW))
                .thenReturn(false);
        when(visitRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);

        // when
        visitService.create(command);

        // then
        verify(visitRepository).saveAndFlush(captor.capture());
        Visit saved = captor.getValue();
        assertEquals(NOW, saved.getStartsAt());
        assertEquals(NOW.plusMinutes(30), saved.getEndsAt());
        assertSame(doctor, saved.getDoctor());
    }

    @Test
    void create_sameSlotSavedByConcurrentRequest_throwsVisitOverlaps() {
        // given
        VisitCreateCommand command = new VisitCreateCommand(1L, NOW, NOW.plusMinutes(30));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(Doctor.builder().id(1L).build()));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(1L, NOW.plusMinutes(30), NOW))
                .thenReturn(false);
        ConstraintViolationException violation = new ConstraintViolationException("duplicate",
                new SQLException("duplicate"), ConstraintKind.UNIQUE, "uq_visits_doctor_id_starts_at");
        when(visitRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate", violation));

        // when
        VisitOverlapsException exception = assertThrows(VisitOverlapsException.class,
                () -> visitService.create(command));

        // then
        assertEquals("Doctor already has a visit in this time slot", exception.getMessage());
    }

    @Test
    void create_otherIntegrityViolation_isNotReportedAsOverlap() {
        // given
        VisitCreateCommand command = new VisitCreateCommand(1L, NOW, NOW.plusMinutes(30));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(Doctor.builder().id(1L).build()));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(1L, NOW.plusMinutes(30), NOW))
                .thenReturn(false);
        ConstraintViolationException violation = new ConstraintViolationException("missing doctor",
                new SQLException("missing doctor"), ConstraintKind.FOREIGN_KEY, "fk_visits_doctor");
        when(visitRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("missing doctor", violation));

        // when + then
        assertThrows(DataIntegrityViolationException.class, () -> visitService.create(command));
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

    @Test
    void findAll_sortByAllowedField_addsIdTieBreakerAndReturnsDtos() {
        // given
        LocalDateTime startsAt = LocalDateTime.of(2030, 3, 4, 10, 0);
        Visit visit = new Visit();
        visit.setId(7L);
        visit.setStartsAt(startsAt);
        visit.setEndsAt(startsAt.plusMinutes(30));
        PageRequest requested = PageRequest.of(0, 5, Sort.by("startsAt"));
        PageRequest stable = PageRequest.of(0, 5, Sort.by("startsAt").and(Sort.by("id")));
        when(visitRepository.findAll(stable)).thenReturn(new PageImpl<>(List.of(visit), stable, 1));

        // when
        Page<VisitDto> result = visitService.findAll(requested);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals(7L, result.getContent().get(0).id());
        assertEquals(startsAt, result.getContent().get(0).startsAt());
    }

    @Test
    void findByPatientId_patientHasVisits_returnsDtosInRepositoryOrder() {
        // given
        LocalDateTime first = LocalDateTime.of(2030, 3, 4, 10, 0);
        Patient patient = new Patient();
        patient.setId(2L);
        Visit earlier = new Visit();
        earlier.setId(8L);
        earlier.setPatient(patient);
        earlier.setStartsAt(first);
        earlier.setEndsAt(first.plusMinutes(30));
        Visit later = new Visit();
        later.setId(3L);
        later.setPatient(patient);
        later.setStartsAt(first.plusDays(1));
        later.setEndsAt(first.plusDays(1).plusMinutes(15));
        when(patientRepository.existsById(2L)).thenReturn(true);
        when(visitRepository.findByPatientIdOrderByStartsAtAscIdAsc(2L)).thenReturn(List.of(earlier, later));

        // when
        List<VisitDto> result = visitService.findByPatientId(2L);

        // then
        assertEquals(List.of(8L, 3L), result.stream().map(VisitDto::id).toList());
        assertEquals(2L, result.get(0).patientId());
    }

    @Test
    void findByPatientId_patientHasNoVisits_returnsEmptyList() {
        // given
        when(patientRepository.existsById(2L)).thenReturn(true);
        when(visitRepository.findByPatientIdOrderByStartsAtAscIdAsc(2L)).thenReturn(List.of());

        // when
        List<VisitDto> result = visitService.findByPatientId(2L);

        // then
        assertEquals(List.of(), result);
    }

    @Test
    void findByPatientId_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.existsById(99L)).thenReturn(false);

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> visitService.findByPatientId(99L));

        // then
        assertEquals("Patient with id: 99 not found", exception.getMessage());
        verify(visitRepository, never()).findByPatientIdOrderByStartsAtAscIdAsc(99L);
    }

    @Test
    void findAll_sortByIdDescending_keepsRequestedSort() {
        // given
        PageRequest requested = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "id"));
        when(visitRepository.findAll(requested)).thenReturn(new PageImpl<>(List.of(), requested, 0));

        // when
        visitService.findAll(requested);

        // then
        verify(visitRepository).findAll(requested);
    }

    @Test
    void findAll_sortByFieldOutsideList_throwsInvalidSortProperty() {
        // given
        PageRequest pageable = PageRequest.of(0, 5, Sort.by("abrakadabra"));

        // when
        InvalidSortPropertyException exception = assertThrows(InvalidSortPropertyException.class,
                () -> visitService.findAll(pageable));

        // then
        assertEquals("cannot sort by 'abrakadabra', allowed: startsAt, endsAt, id", exception.getMessage());
        verify(visitRepository, never()).findAll(any(PageRequest.class));
    }
}
