package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class VisitOverlapsException extends MedicalClinicException {
    public VisitOverlapsException() {
        super("Doctor already has a visit in this time slot", HttpStatus.CONFLICT);
    }
}
