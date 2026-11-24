package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class SelfSubstitutionException extends MedicalClinicException {
    public SelfSubstitutionException(Long doctorId) {
        super("Doctor with id: " + doctorId + " cannot substitute for themselves", HttpStatus.BAD_REQUEST);
    }
}
