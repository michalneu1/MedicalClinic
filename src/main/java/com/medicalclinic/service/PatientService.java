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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper patientMapper;


    public List<PatientDto> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .toList();
    }

    public PatientDto findById(Long id) {
        return patientRepository.findById(id)
                .map(patientMapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public PatientDto findByEmail(String email) {
        return patientRepository.findByUserEmail(email)
                .map(patientMapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException(email));
    }

    @Transactional
    public PatientDto create(PatientCreateCommand command) {
        //Business validation repository must be checked if email already exist
        if (userRepository.existsByEmail(command.email())) {
            throw new PatientAlreadyExistsException();
        }
        User user = userRepository.save(User.builder()
                .email(command.email())
                .password(command.password())
                .role(Role.PATIENT)
                .build());
        Patient patient = patientMapper.toEntity(command);
        patient.setUser(user);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    public PatientDto update(Long id, PatientUpdateCommand newData) {
        return patientRepository.findById(id)
                .map(existing -> {
                    patientMapper.updateEntity(newData, existing);
                    return patientMapper.toDto(patientRepository.save(existing));
                }).orElseThrow(() -> new PatientNotFoundException(id));
    }

    public void deleteById(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        patientRepository.deleteById(id);
    }

    @Transactional
    public PatientDto updatePassword(Long id, String password) {
        Patient patient = patientRepository
                .findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        patient.getUser().setPassword(password);
        return patientMapper.toDto(patient);
    }

    public List<PatientDto> searchByFragment(String fragment) {
        return patientRepository.searchByNameFragment(fragment).stream()
                .map(patientMapper::toDto)
                .toList();
    }
}
