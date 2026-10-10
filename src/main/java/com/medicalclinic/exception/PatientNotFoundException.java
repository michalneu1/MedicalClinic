package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class PatientNotFoundException extends MedicalClinicException {
    public PatientNotFoundException(Long id) {
        super("Patient with id: " + id + " not found", HttpStatus.NOT_FOUND);
    }

    public PatientNotFoundException(String email) {
        super("Patient with email: " + email + " not found", HttpStatus.NOT_FOUND);
    }
}
