package com.medicalclinic.exception;

import com.medicalclinic.model.Role;
import org.springframework.http.HttpStatus;

public class UserNotDoctorException extends MedicalClinicException {
    public UserNotDoctorException(Long userId, Role role) {
        super("User with id: " + userId + " has role " + role
                + ", doctor profile requires role " + Role.DOCTOR, HttpStatus.CONFLICT);
    }
}
