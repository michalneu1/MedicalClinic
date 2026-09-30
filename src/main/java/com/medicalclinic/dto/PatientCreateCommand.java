package com.medicalclinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientCreateCommand(
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 8)
        String password,
        @NotBlank
        @Pattern(regexp = "[A-Z]{3}\\d{6}")
        String idCardNo,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        @Pattern(regexp = "\\d{9}")
        String phoneNumber,
        @Past
        LocalDate birthday
) {
}
