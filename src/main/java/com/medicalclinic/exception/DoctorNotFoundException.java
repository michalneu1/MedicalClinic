package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorNotFoundException extends MedicalClinicException {
    public DoctorNotFoundException(Long id) {
        super("Doctor with id: " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
