package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class PatientAlreadyExistsException extends MedicalClinicException {
    private PatientAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public static PatientAlreadyExistsException emailTaken() {
        return new PatientAlreadyExistsException("Patient with this email already exists");
    }

    public static PatientAlreadyExistsException idCardNoTaken() {
        return new PatientAlreadyExistsException("Patient with this id card number already exists");
    }
}
