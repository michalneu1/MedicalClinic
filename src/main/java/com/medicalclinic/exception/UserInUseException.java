package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class UserInUseException extends MedicalClinicException {
    public UserInUseException(Long id) {
        super("User with id: " + id + " is in use and cannot be deleted", HttpStatus.CONFLICT);
    }
}
