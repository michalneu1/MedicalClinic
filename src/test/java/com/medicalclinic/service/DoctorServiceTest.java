package com.medicalclinic.service;

import com.medicalclinic.dto.doctor.DoctorCreateCommand;
import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.dto.doctor.SubstitutionCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.NoVisitsToSubstituteException;
import com.medicalclinic.exception.SelfSubstitutionException;
import com.medicalclinic.exception.UserNotDoctorException;
import com.medicalclinic.exception.VisitOverlapsException;
import com.medicalclinic.mapper.DoctorMapper;
import com.medicalclinic.mapper.VisitMapper;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Role;
import com.medicalclinic.model.Specialization;
import com.medicalclinic.model.User;
import com.medicalclinic.model.Visit;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.FacilityRepository;
import com.medicalclinic.repository.UserRepository;
import com.medicalclinic.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 15, 8, 0);

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VisitRepository visitRepository;
    @Mock
    private FacilityRepository facilityRepository;

    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        doctorService = new DoctorService(doctorRepository, userRepository, visitRepository, facilityRepository,
                Mappers.getMapper(DoctorMapper.class), Mappers.getMapper(VisitMapper.class), clock);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"PATIENT", "ADMIN"})
    void create_userWithoutDoctorRole_throwsUserNotDoctor(Role role) {
        // given
        User user = User.builder().id(5L).email("jan@example.com").role(role).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        DoctorCreateCommand command = new DoctorCreateCommand(5L, "Jan", "Kowalski", Specialization.CARDIOLOGIST);

        // when
        UserNotDoctorException exception = assertThrows(UserNotDoctorException.class,
                () -> doctorService.create(command));

        // then
        assertEquals("User with id: 5 has role " + role + ", doctor profile requires role DOCTOR",
                exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_userWithDoctorRole_savesDoctor() {
        // given
        User user = User.builder().id(5L).email("anna@example.com").role(Role.DOCTOR).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUserId(5L)).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor doctor = invocation.getArgument(0);
            doctor.setId(9L);
            return doctor;
        });
        DoctorCreateCommand command = new DoctorCreateCommand(5L, "Anna", "Nowak", Specialization.CARDIOLOGIST);

        // when
        DoctorDto result = doctorService.create(command);

        // then
        assertEquals(9L, result.id());
        assertEquals(5L, result.userId());
    }

    @Test
    void substitute_doctorForThemselves_throwsSelfSubstitution() {
        // given
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor(1L)));

        // when
        SelfSubstitutionException exception = assertThrows(SelfSubstitutionException.class,
                () -> doctorService.substitute(1L, new SubstitutionCommand(1L)));

        // then
        assertEquals("Doctor with id: 1 cannot substitute for themselves", exception.getMessage());
        verifyNoInteractions(visitRepository);
    }

    @Test
    void substitute_noUpcomingVisits_throwsNoVisitsToSubstitute() {
        // given
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor(1L)));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor(2L)));
        when(visitRepository.findByDoctorIdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(1L, NOW))
                .thenReturn(List.of());

        // when
        NoVisitsToSubstituteException exception = assertThrows(NoVisitsToSubstituteException.class,
                () -> doctorService.substitute(1L, new SubstitutionCommand(2L)));

        // then
        assertEquals("Doctor with id: 1 has no upcoming visits to hand over", exception.getMessage());
    }

    @Test
    void substitute_upcomingVisits_movesThemToSubstitute() {
        // given
        Doctor absent = doctor(1L);
        Doctor substitute = doctor(2L);
        Visit visit = visit(10L, absent, NOW.plusDays(1));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(absent));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(substitute));
        when(visitRepository.findByDoctorIdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(1L, NOW))
                .thenReturn(List.of(visit));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(
                2L, visit.getEndsAt(), visit.getStartsAt())).thenReturn(false);

        // when
        List<VisitDto> result = doctorService.substitute(1L, new SubstitutionCommand(2L));

        // then
        assertEquals(1, result.size());
        assertSame(substitute, visit.getDoctor());
    }

    @Test
    void substitute_substituteBusy_throwsVisitOverlaps() {
        // given
        Doctor absent = doctor(1L);
        Visit visit = visit(10L, absent, NOW.plusDays(1));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(absent));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor(2L)));
        when(visitRepository.findByDoctorIdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(1L, NOW))
                .thenReturn(List.of(visit));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(
                2L, visit.getEndsAt(), visit.getStartsAt())).thenReturn(true);

        // when
        assertThrows(VisitOverlapsException.class,
                () -> doctorService.substitute(1L, new SubstitutionCommand(2L)));

        // then
        assertSame(absent, visit.getDoctor());
    }

    private Doctor doctor(Long id) {
        return Doctor.builder().id(id).build();
    }

    private Visit visit(Long id, Doctor doctor, LocalDateTime startsAt) {
        return Visit.builder().id(id).doctor(doctor).startsAt(startsAt).endsAt(startsAt.plusMinutes(30)).build();
    }
}
