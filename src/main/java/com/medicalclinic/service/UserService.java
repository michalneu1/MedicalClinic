package com.medicalclinic.service;

import com.medicalclinic.dto.user.UserCreateCommand;
import com.medicalclinic.dto.user.UserDto;
import com.medicalclinic.exception.UserAlreadyExistsException;
import com.medicalclinic.exception.UserInUseException;
import com.medicalclinic.exception.UserNotFoundException;
import com.medicalclinic.mapper.UserMapper;
import com.medicalclinic.model.User;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.PatientRepository;
import com.medicalclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public UserDto create(UserCreateCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }
        User saved = userRepository.save(userMapper.toEntity(command));
        log.info("created user {}", saved.getId());
        return userMapper.toDto(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        if (patientRepository.existsByUserId(id) || doctorRepository.existsByUserId(id)) {
            throw new UserInUseException(id);
        }
        userRepository.deleteById(id);
        log.info("deleted user {}", id);
    }
}
