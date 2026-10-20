package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class VisitNotFoundException extends MedicalClinicException {
    public VisitNotFoundException(Long id) {
        super("Visit with id: " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
