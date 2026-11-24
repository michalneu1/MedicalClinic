package com.medicalclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientUpdateCommand(
        @NotBlank(message = "first name is required")
        @Size(max = 100, message = "first name must be at most 100 characters long")
        String firstName,
        @NotBlank(message = "last name is required")
        @Size(max = 100, message = "last name must be at most 100 characters long")
        String lastName,
        @NotBlank(message = "phone number is required")
        @Pattern(regexp = "\\d{9}", message = "phone number must be exactly 9 digits")
        String phoneNumber,
        @NotNull(message = "birthday is required")
        @Past(message = "birthday must be a date in the past")
        LocalDate birthday
) {
}
