package com.medicalclinic.dto.doctor;

import com.medicalclinic.model.Specialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DoctorCreateCommand(
        @NotNull(message = "user id is required")
        Long userId,
        @NotBlank(message = "first name is required")
        String firstName,
        @NotBlank(message = "last name is required")
        String lastName,
        @NotNull(message = "specialization is required")
        Specialization specialization
) {
}
