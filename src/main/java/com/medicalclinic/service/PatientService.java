package com.medicalclinic.service;

import com.medicalclinic.exception.PatientAlreadyExistsException;
import com.medicalclinic.model.Patient;
import com.medicalclinic.repository.InMemoryPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final InMemoryPatientRepository repository;


    public List<Patient> findAll() {
        return repository.findAll();
    }

    public Optional<Patient> findById(Long id) {
        return repository.findById(id);
    }

    public Patient create(Patient patient) {
        if (repository.findByEmail(patient.getEmail()).isPresent()) {
            throw new PatientAlreadyExistsException();
        }
        return repository.save(patient);

    }

    public Optional<Patient> update(Long id, Patient newData) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setEmail(newData.getEmail());
                    existing.setPassword(newData.getPassword());
                    existing.setIdCardNo(newData.getIdCardNo());
                    existing.setFirstName(newData.getFirstName());
                    existing.setLastName(newData.getLastName());
                    existing.setPhoneNumber(newData.getPhoneNumber());
                    existing.setBirthday(newData.getBirthday());
                    return existing;
                });
    }

    public boolean deleteById(Long id) {
        return repository.deleteById(id);
    }


}
