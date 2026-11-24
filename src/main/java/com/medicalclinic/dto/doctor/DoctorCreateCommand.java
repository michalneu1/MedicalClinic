package com.medicalclinic.dto.doctor;

import com.medicalclinic.model.Specialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DoctorCreateCommand(
        @NotNull(message = "user id is required")
        Long userId,
        @NotBlank(message = "first name is required")
        @Size(max = 100, message = "first name must be at most 100 characters long")
        String firstName,
        @NotBlank(message = "last name is required")
        @Size(max = 100, message = "last name must be at most 100 characters long")
        String lastName,
        @NotNull(message = "specialization is required")
        Specialization specialization
) {
}
