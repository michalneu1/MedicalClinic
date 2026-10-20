package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorAlreadyExistsException extends MedicalClinicException {
    public DoctorAlreadyExistsException(Long userId) {
        super("User with id: " + userId + " already has a doctor", HttpStatus.CONFLICT);
    }
}
