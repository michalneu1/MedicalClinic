package com.medicalclinic.service;

import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.PatientUpdateCommand;
import com.medicalclinic.exception.PatientAlreadyExistsException;
import com.medicalclinic.exception.PatientNotFoundException;
import com.medicalclinic.mapper.PatientMapper;
import com.medicalclinic.model.Patient;
import com.medicalclinic.model.Role;
import com.medicalclinic.model.User;
import com.medicalclinic.repository.PatientRepository;
import com.medicalclinic.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientMapper patientMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientService patientService;

    @Test
    void findById_patientExists_returnsPatient() {
        // given
        Patient patient = Patient.builder().id(5L).firstName("Jan").lastName("Kowalski").build();
        PatientDto dto = new PatientDto(5L, "jan@example.com", "Jan", "Kowalski", null, null);
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(dto);

        // when
        PatientDto result = patientService.findById(5L);

        // then
        assertEquals("Jan", result.firstName());
    }

    @Test
    void findAll_twoPatientsExist_returnsBoth() {
        // given
        Patient jan = Patient.builder().id(1L).firstName("Jan").lastName("Kowalski").build();
        Patient anna = Patient.builder().id(2L).firstName("Anna").lastName("Nowak").build();
        when(patientRepository.findAll()).thenReturn(List.of(jan, anna));
        when(patientMapper.toDto(jan)).thenReturn(new PatientDto(1L, "jan@example.com", "Jan", "Kowalski", null, null));
        when(patientMapper.toDto(anna)).thenReturn(new PatientDto(2L, "anna@example.com", "Anna", "Nowak", null, null));

        // when
        List<PatientDto> result = patientService.findAll();

        // then
        assertEquals(2, result.size());
        assertEquals("Anna", result.get(1).firstName());
    }

    @Test
    void findByEmail_patientExists_returnsPatient() {
        // given
        Patient patient = Patient.builder().id(5L).firstName("Jan").lastName("Kowalski").build();
        when(patientRepository.findByUserEmail("jan@example.com")).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(new PatientDto(5L, "jan@example.com", "Jan", "Kowalski", null, null));

        // when
        PatientDto result = patientService.findByEmail("jan@example.com");

        // then
        assertEquals(5L, result.id());
    }

    @Test
    void searchByFragment_lastNameFragmentMatches_returnsOnePatient() {
        // given
        Patient patient = Patient.builder().id(5L).firstName("Jan").lastName("Kowalski").build();
        when(patientRepository.searchByNameFragment("kowal")).thenReturn(List.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(new PatientDto(5L, "jan@example.com", "Jan", "Kowalski", null, null));

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
        Patient patient = Patient.builder().firstName("Jan").lastName("Kowalski").build();
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(patientMapper.toEntity(command)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(passwordEncoder.encode("tajneHaslo1")).thenReturn("$2a$10$zakodowane");

        // when
        patientService.create(command);

        // then
        User user = patient.getUser();
        assertEquals("jan@example.com", user.getEmail());
        assertEquals("$2a$10$zakodowane", user.getPassword());
        assertEquals(Role.PATIENT, user.getRole());
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
        assertEquals("Patient already exists", exception.getMessage());
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
    void deleteById_patientExists_deletesPatient() {
        // given
        when(patientRepository.existsById(5L)).thenReturn(true);

        // when
        patientService.deleteById(5L);

        // then
        verify(patientRepository).deleteById(5L);
    }

    @Test
    void deleteById_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.existsById(99L)).thenReturn(false);

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.deleteById(99L));

        // then
        assertEquals("Patient with id: 99 not found", exception.getMessage());
        verify(patientRepository, never()).deleteById(any());
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
    void update_patientExists_updatesPatientWithMapper() {
        // given
        Patient patient = Patient.builder().id(5L).firstName("Jan").lastName("Kowalski").build();
        PatientUpdateCommand command = new PatientUpdateCommand("Jan", "Nowak", "987654321",
                LocalDate.of(1990, 1, 1));
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(new PatientDto(5L, "jan@example.com", "Jan", "Nowak", null, null));

        // when
        patientService.update(5L, command);

        // then
        verify(patientMapper).updateEntity(command, patient);
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
        verify(patientMapper, never()).updateEntity(any(), any());
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

    @Test
    void create_newEmail_savesPatientCapturedWithAccount() {
        // given
        PatientCreateCommand command = new PatientCreateCommand("jan@example.com", "tajneHaslo1", "ABC123456",
                "Jan", "Kowalski", "123456789", LocalDate.of(1990, 1, 1));
        Patient patient = Patient.builder().firstName("Jan").lastName("Kowalski").build();
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(patientMapper.toEntity(command)).thenReturn(patient);
        when(patientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);

        // when
        patientService.create(command);

        // then
        verify(patientRepository).save(captor.capture());
        Patient saved = captor.getValue();
        assertEquals("Jan", saved.getFirstName());
        assertEquals("jan@example.com", saved.getUser().getEmail());
        assertEquals(Role.PATIENT, saved.getUser().getRole());
    }
}
