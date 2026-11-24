package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class AmbiguousPatientQueryException extends MedicalClinicException {
    public AmbiguousPatientQueryException() {
        super("Use either 'email' or 'fragment', not both", HttpStatus.BAD_REQUEST);
    }
}
