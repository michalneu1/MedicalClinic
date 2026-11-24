package com.medicalclinic.service;

import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.PatientUpdateCommand;
import com.medicalclinic.exception.PatientAlreadyExistsException;
import com.medicalclinic.exception.PatientHasVisitsException;
import com.medicalclinic.exception.PatientNotFoundException;
import com.medicalclinic.mapper.PatientMapper;
import com.medicalclinic.model.Patient;
import com.medicalclinic.model.Role;
import com.medicalclinic.model.User;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.PatientRepository;
import com.medicalclinic.repository.UserRepository;
import com.medicalclinic.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VisitRepository visitRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository, userRepository, visitRepository, doctorRepository,
                Mappers.getMapper(PatientMapper.class), passwordEncoder);
    }

    @Test
    void findById_patientExists_returnsPatient() {
        // given
        when(patientRepository.findById(5L)).thenReturn(Optional.of(jan()));

        // when
        PatientDto result = patientService.findById(5L);

        // then
        assertEquals(new PatientDto(5L, "jan@example.com", "Jan", "Kowalski", "123456789",
                LocalDate.of(1990, 1, 1)), result);
    }

    @Test
    void findAll_twoPatientsExist_returnsBoth() {
        // given
        User annaUser = User.builder().email("anna@example.com").role(Role.PATIENT).build();
        Patient anna = Patient.builder().id(2L).user(annaUser).firstName("Anna").lastName("Nowak").build();
        when(patientRepository.findAll()).thenReturn(List.of(jan(), anna));

        // when
        List<PatientDto> result = patientService.findAll();

        // then
        assertEquals(2, result.size());
        assertEquals("Anna", result.get(1).firstName());
        assertEquals("anna@example.com", result.get(1).email());
    }

    @Test
    void findByEmail_patientExists_returnsPatient() {
        // given
        when(patientRepository.findByUserEmail("jan@example.com")).thenReturn(Optional.of(jan()));

        // when
        PatientDto result = patientService.findByEmail("jan@example.com");

        // then
        assertEquals(5L, result.id());
        assertEquals("jan@example.com", result.email());
    }

    @Test
    void searchByFragment_lastNameFragmentMatches_returnsOnePatient() {
        // given
        when(patientRepository.searchByNameFragment("kowal")).thenReturn(List.of(jan()));

        // when
        List<PatientDto> result = patientService.searchByFragment("kowal");

        // then
        assertEquals(1, result.size());
        assertEquals("Kowalski", result.get(0).lastName());
    }

    @Test
    void create_newEmail_savesPatientWithAccount() {
        // given
        PatientCreateCommand command = new PatientCreateCommand("jan@example.com", "tajneHaslo1", "ABC123456",
                "Jan", "Kowalski", "123456789", LocalDate.of(1990, 1, 1));
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(patientRepository.existsByIdCardNo("ABC123456")).thenReturn(false);
        when(patientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("tajneHaslo1")).thenReturn("$2a$10$zakodowane");
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);

        // when
        PatientDto result = patientService.create(command);

        // then
        verify(patientRepository).save(captor.capture());
        Patient saved = captor.getValue();
        assertEquals("Jan", saved.getFirstName());
        assertEquals("Kowalski", saved.getLastName());
        assertEquals("ABC123456", saved.getIdCardNo());
        assertEquals("123456789", saved.getPhoneNumber());
        assertEquals(LocalDate.of(1990, 1, 1), saved.getBirthday());
        assertEquals("jan@example.com", saved.getUser().getEmail());
        assertEquals("$2a$10$zakodowane", saved.getUser().getPassword());
        assertEquals(Role.PATIENT, saved.getUser().getRole());
        assertEquals("jan@example.com", result.email());
    }

    @Test
    void updatePassword_patientExists_setsNewPassword() {
        // given
        User user = User.builder().email("jan@example.com").password("stareHaslo").role(Role.PATIENT).build();
        Patient patient = Patient.builder().id(5L).user(user).firstName("Jan").lastName("Kowalski").build();
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
        when(passwordEncoder.encode("noweHaslo1")).thenReturn("$2a$10$noweZakodowane");

        // when
        patientService.updatePassword(5L, "noweHaslo1");

        // then
        assertEquals("$2a$10$noweZakodowane", patient.getUser().getPassword());
    }

    @Test
    void create_emailTaken_throwsPatientAlreadyExists() {

        // given
        PatientCreateCommand command = new PatientCreateCommand(
                "jan@example.com",
                "tajneHaslo1",
                "ABC123456",
                "Jan",
                "Kowalski",
                "123456789",
                LocalDate.of(1990, 1, 1)
        );
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(true);
        // when
        PatientAlreadyExistsException exception = assertThrows(PatientAlreadyExistsException.class,
                () -> patientService.create(command));

        // then
        assertEquals("Patient with this email already exists", exception.getMessage());
        verify(patientRepository, never()).save(any());

    }

    @Test
    void create_idCardNoTaken_throwsPatientAlreadyExists() {
        // given
        PatientCreateCommand command = new PatientCreateCommand("jan@example.com", "tajneHaslo1", "ABC123456",
                "Jan", "Kowalski", "123456789", LocalDate.of(1990, 1, 1));
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(patientRepository.existsByIdCardNo("ABC123456")).thenReturn(true);

        // when
        PatientAlreadyExistsException exception = assertThrows(PatientAlreadyExistsException.class,
                () -> patientService.create(command));

        // then
        assertEquals("Patient with this id card number already exists", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void findById_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.findById(99L));

        // then
        assertEquals("Patient with id: 99 not found", exception.getMessage());
    }

    @Test
    void deleteById_patientWithoutVisits_deletesPatientAndAccount() {
        // given
        User user = User.builder().id(7L).email("jan@example.com").role(Role.PATIENT).build();
        Patient patient = Patient.builder().id(5L).user(user).build();
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
        when(visitRepository.existsByPatientId(5L)).thenReturn(false);
        when(doctorRepository.existsByUserId(7L)).thenReturn(false);

        // when
        patientService.deleteById(5L);

        // then
        verify(patientRepository).delete(patient);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteById_accountHasDoctorProfile_deletesPatientKeepsAccount() {
        // given
        User user = User.builder().id(7L).email("katarzyna@example.com").role(Role.DOCTOR).build();
        Patient patient = Patient.builder().id(5L).user(user).build();
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
        when(visitRepository.existsByPatientId(5L)).thenReturn(false);
        when(doctorRepository.existsByUserId(7L)).thenReturn(true);

        // when
        patientService.deleteById(5L);

        // then
        verify(patientRepository).delete(patient);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteById_patientHasVisits_throwsPatientHasVisits() {
        // given
        Patient patient = Patient.builder().id(5L).build();
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
        when(visitRepository.existsByPatientId(5L)).thenReturn(true);

        // when
        PatientHasVisitsException exception = assertThrows(PatientHasVisitsException.class,
                () -> patientService.deleteById(5L));

        // then
        assertEquals("Patient with id: 5 has visits and cannot be deleted", exception.getMessage());
        verify(patientRepository, never()).delete(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteById_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.deleteById(99L));

        // then
        assertEquals("Patient with id: 99 not found", exception.getMessage());
        verify(patientRepository, never()).delete(any());
    }

    @Test
    void findByEmail_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.findByUserEmail("brak@example.com")).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.findByEmail("brak@example.com"));

        // then
        assertEquals("Patient with email: brak@example.com not found", exception.getMessage());
    }

    @Test
    void update_patientExists_changesDataAndKeepsIdCardNoAndAccount() {
        // given
        Patient patient = jan();
        User account = patient.getUser();
        PatientUpdateCommand command = new PatientUpdateCommand("Janusz", "Nowak", "987654321",
                LocalDate.of(1991, 2, 3));
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));

        // when
        PatientDto result = patientService.update(5L, command);

        // then
        assertEquals("Janusz", patient.getFirstName());
        assertEquals("Nowak", patient.getLastName());
        assertEquals("987654321", patient.getPhoneNumber());
        assertEquals(LocalDate.of(1991, 2, 3), patient.getBirthday());
        assertEquals(5L, patient.getId());
        assertEquals("ABC123456", patient.getIdCardNo());
        assertSame(account, patient.getUser());
        assertEquals(new PatientDto(5L, "jan@example.com", "Janusz", "Nowak", "987654321",
                LocalDate.of(1991, 2, 3)), result);
    }

    @Test
    void update_patientNotFound_throwsPatientNotFound() {
        // given
        PatientUpdateCommand command = new PatientUpdateCommand("Jan", "Nowak", "987654321",
                LocalDate.of(1990, 1, 1));
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.update(99L, command));

        // then
        assertEquals("Patient with id: 99 not found", exception.getMessage());
    }

    @Test
    void updatePassword_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.updatePassword(99L, "noweHaslo1"));

        // then
        assertEquals("Patient with id: 99 not found", exception.getMessage());
    }

    private Patient jan() {
        User user = User.builder().id(7L).email("jan@example.com").password("stareHaslo").role(Role.PATIENT).build();
        return Patient.builder().id(5L).user(user).idCardNo("ABC123456").firstName("Jan").lastName("Kowalski")
                .phoneNumber("123456789").birthday(LocalDate.of(1990, 1, 1)).build();
    }
}
