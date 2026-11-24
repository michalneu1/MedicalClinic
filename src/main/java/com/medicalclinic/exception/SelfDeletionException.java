package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class SelfDeletionException extends MedicalClinicException {
    public SelfDeletionException(Long id) {
        super("User with id: " + id + " cannot delete their own account", HttpStatus.CONFLICT);
    }
}
