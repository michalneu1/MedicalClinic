package com.medicalclinic.repository;

import com.medicalclinic.model.Patient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPatientRepository {
    private final ArrayList<Patient> list = new ArrayList<>();
    private final AtomicLong id = new AtomicLong(1);

    public List<Patient> findAll() {
        return List.copyOf(list);
    }

    public void createPatient(Patient patient) {
        list.add(patient);
    }

    public Optional<Patient> findById(Long id) {
        return list.stream().
                filter(patient -> patient.getId().equals(id)).findFirst();
    }
    public Optional<Patient> findByEmail(String email) {
        return list.stream().
                filter(patient -> patient.getEmail().equals(email)).findFirst();
    }

    public Patient save(Patient patient) {
        if (patient.getId() == null) {
            patient.setId(id.getAndIncrement());
            list.add(patient);
        }
        return patient;
    }

    public boolean deleteById(Long id) {
        return list.removeIf(patient -> patient.getId().equals(id));
    }

}
