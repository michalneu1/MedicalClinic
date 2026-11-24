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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientMapper patientMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<PatientDto> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientDto findById(Long id) {
        return patientRepository.findById(id)
                .map(patientMapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public PatientDto findByEmail(String email) {
        return patientRepository.findByUserEmail(email)
                .map(patientMapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException(email));
    }

    @Transactional
    public PatientDto create(PatientCreateCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw PatientAlreadyExistsException.emailTaken();
        }
        if (patientRepository.existsByIdCardNo(command.idCardNo())) {
            throw PatientAlreadyExistsException.idCardNoTaken();
        }
        User user = User.builder()
                .email(command.email())
                .password(passwordEncoder.encode(command.password()))
                .role(Role.PATIENT)
                .build();
        Patient patient = patientMapper.toEntity(command);
        patient.setUser(user);
        Patient saved = patientRepository.save(patient);
        log.info("registered patient {}", saved.getId());
        return patientMapper.toDto(saved);
    }

    @Transactional
    public PatientDto update(Long id, PatientUpdateCommand newData) {
        return patientRepository.findById(id)
                .map(existing -> {
                    patientMapper.updateEntity(newData, existing);
                    log.info("updated patient {}", id);
                    return patientMapper.toDto(existing);
                }).orElseThrow(() -> new PatientNotFoundException(id));
    }

    @Transactional
    public void deleteById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        if (visitRepository.existsByPatientId(id)) {
            throw new PatientHasVisitsException(id);
        }
        User user = patient.getUser();
        patientRepository.delete(patient);
        if (doctorRepository.existsByUserId(user.getId())) {
            log.info("deleted patient {}, user {} kept for doctor profile", id, user.getId());
            return;
        }
        userRepository.delete(user);
        log.info("deleted patient {} with user {}", id, user.getId());
    }

    @Transactional
    public PatientDto updatePassword(Long id, String password) {
        Patient patient = patientRepository
                .findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        patient.getUser().setPassword(passwordEncoder.encode(password));
        log.info("changed password of patient {}", id);
        return patientMapper.toDto(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientDto> searchByFragment(String fragment) {
        return patientRepository.searchByNameFragment(fragment).stream()
                .map(patientMapper::toDto)
                .toList();
    }
}
