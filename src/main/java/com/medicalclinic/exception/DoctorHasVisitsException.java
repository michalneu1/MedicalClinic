package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorHasVisitsException extends MedicalClinicException {
    public DoctorHasVisitsException(Long id) {
        super("Doctor with id: " + id + " has visits and cannot be deleted", HttpStatus.CONFLICT);
    }
}
