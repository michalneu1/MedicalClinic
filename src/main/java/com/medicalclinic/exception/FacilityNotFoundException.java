package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class FacilityNotFoundException extends MedicalClinicException {
    public FacilityNotFoundException(Long id) {
        super("Facility with id: " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
