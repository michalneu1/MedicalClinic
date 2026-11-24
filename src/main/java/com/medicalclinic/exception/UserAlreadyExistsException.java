package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends MedicalClinicException {
    public UserAlreadyExistsException(String email) {
        super("User with email: " + email + " already exists", HttpStatus.CONFLICT);
    }
}
