package com.medicalclinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientCreateCommand(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,
        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters long")
        String password,
        @NotBlank(message = "id card number is required")
        @Pattern(regexp = "[A-Z]{3}\\d{6}", message = "id card number must be 3 uppercase letters followed by 6 digits")
        String idCardNo,
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
