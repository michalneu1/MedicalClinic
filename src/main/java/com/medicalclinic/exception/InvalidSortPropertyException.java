package com.medicalclinic.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class InvalidSortPropertyException extends MedicalClinicException {
    public InvalidSortPropertyException(String property, List<String> allowed) {
        super("cannot sort by '" + property + "', allowed: " + String.join(", ", allowed),
                HttpStatus.BAD_REQUEST);
    }
}
