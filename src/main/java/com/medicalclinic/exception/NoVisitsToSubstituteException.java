package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class NoVisitsToSubstituteException extends MedicalClinicException {
    public NoVisitsToSubstituteException(Long doctorId) {
        super("Doctor with id: " + doctorId + " has no upcoming visits to hand over", HttpStatus.CONFLICT);
    }
}
