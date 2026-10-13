package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class UserInUseException extends MedicalClinicException {
    public UserInUseException(Long id) {
        super("User with id: " + id + " is assigned to a patient and cannot be deleted", HttpStatus.CONFLICT);
    }
}
