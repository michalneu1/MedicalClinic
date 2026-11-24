package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class VisitAlreadyBookedException extends MedicalClinicException {
    public VisitAlreadyBookedException(Long id) {
        super("Visit with id: " + id + " is already booked", HttpStatus.CONFLICT);
    }
}
