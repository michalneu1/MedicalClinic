package com.medicalclinic.service;

import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.PatientUpdateCommand;
import com.medicalclinic.exception.PatientAlreadyExistsException;
import com.medicalclinic.exception.PatientNotFoundException;
import com.medicalclinic.mapper.PatientMapper;
import com.medicalclinic.model.Patient;
import com.medicalclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;
    private final PatientMapper patientMapper;


    public List<PatientDto> findAll() {
        return repository.findAll().stream()
                .map(patientMapper::toDto)
                .toList();
    }

    public PatientDto findById(Long id) {
        return repository.findById(id)
                .map(patientMapper::toDto)
                .orElseThrow(()-> new PatientNotFoundException(id));
    }

    public PatientDto findByEmail(String email) {
        return repository.findByEmail(email)
                .map(patientMapper::toDto)
                .orElseThrow(()-> new PatientNotFoundException(email));
    }

    public PatientDto create(PatientCreateCommand command) {
        //Business validation repository must be checked if email already exist
        if (repository.existsByEmail(command.email())) {
            throw new PatientAlreadyExistsException();
        }
        Patient patient = patientMapper.toEntity(command);
        return patientMapper.toDto(repository.save(patient));
    }

    public PatientDto update(Long id, PatientUpdateCommand newData) {
        return repository.findById(id)
                .map(existing -> {
                    patientMapper.updateEntity(newData, existing);
                    return patientMapper.toDto(repository.save(existing));
                }).orElseThrow(()-> new PatientNotFoundException(id));
    }

    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public PatientDto updatePassword(Long id, String password) {
        return repository.findById(id).map(existing -> {
            existing.setPassword(password);
            return patientMapper.toDto(repository.save(existing));
        }).orElseThrow(()-> new PatientNotFoundException(id));
    }

    public List<PatientDto> searchByFragment(String fragment) {
        return repository.searchByNameFragment(fragment).stream()
                .map(patientMapper::toDto)
                .toList();
    }
}
