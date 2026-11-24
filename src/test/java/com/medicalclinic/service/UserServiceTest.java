package com.medicalclinic.service;

import com.medicalclinic.exception.SelfDeletionException;
import com.medicalclinic.exception.UserInUseException;
import com.medicalclinic.exception.UserNotFoundException;
import com.medicalclinic.mapper.UserMapper;
import com.medicalclinic.model.Role;
import com.medicalclinic.model.User;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.PatientRepository;
import com.medicalclinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String ADMIN_EMAIL = "admin";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, patientRepository, doctorRepository,
                Mappers.getMapper(UserMapper.class), passwordEncoder);
    }

    @Test
    void deleteById_ownAccount_throwsSelfDeletion() {
        // given
        User admin = User.builder().id(1L).email(ADMIN_EMAIL).role(Role.ADMIN).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        // when
        SelfDeletionException exception = assertThrows(SelfDeletionException.class,
                () -> userService.deleteById(1L, ADMIN_EMAIL));

        // then
        assertEquals("User with id: 1 cannot delete their own account", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteById_otherAccountWithoutProfiles_deletesUser() {
        // given
        User other = User.builder().id(7L).email("jan@example.com").role(Role.PATIENT).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(other));
        when(patientRepository.existsByUserId(7L)).thenReturn(false);
        when(doctorRepository.existsByUserId(7L)).thenReturn(false);

        // when
        userService.deleteById(7L, ADMIN_EMAIL);

        // then
        verify(userRepository).delete(other);
    }

    @Test
    void deleteById_accountWithPatient_throwsUserInUse() {
        // given
        User other = User.builder().id(7L).email("jan@example.com").role(Role.PATIENT).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(other));
        when(patientRepository.existsByUserId(7L)).thenReturn(true);

        // when
        assertThrows(UserInUseException.class, () -> userService.deleteById(7L, ADMIN_EMAIL));

        // then
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteById_unknownId_throwsUserNotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        assertThrows(UserNotFoundException.class, () -> userService.deleteById(99L, ADMIN_EMAIL));

        // then
        verify(userRepository, never()).delete(any());
    }
}
