package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class PatientHasVisitsException extends MedicalClinicException {
    public PatientHasVisitsException(Long id) {
        super("Patient with id: " + id + " has visits and cannot be deleted", HttpStatus.CONFLICT);
    }
}
