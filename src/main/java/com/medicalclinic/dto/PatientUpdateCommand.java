package com.medicalclinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PatientUpdateCommand(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,
        @NotBlank(message = "first name is required")
        String firstName,
        @NotBlank(message = "last name is required")
        String lastName,
        @NotBlank(message = "phone number is required")
        @Pattern(regexp = "\\d{9}", message = "phone number must be exactly 9 digits")
        String phoneNumber,
        @Past(message = "birthday must be a date in the past")
        LocalDate birthday
) {
}
