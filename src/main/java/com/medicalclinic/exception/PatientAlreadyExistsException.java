package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class PatientAlreadyExistsException extends MedicalClinicException {
    public PatientAlreadyExistsException() {
        super("Patient already exists", HttpStatus.CONFLICT);
    }
}
