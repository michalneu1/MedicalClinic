package com.medicalclinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.medicalclinic.validation.MaxBytes;

import java.time.LocalDate;

public record PatientCreateCommand(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 255, message = "email must be at most 255 characters long")
        String email,
        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters long")
        @MaxBytes(value = 72, message = "password must be at most 72 bytes long")
        String password,
        @NotBlank(message = "id card number is required")
        @Pattern(regexp = "[A-Z]{3}\\d{6}", message = "id card number must be 3 uppercase letters followed by 6 digits")
        String idCardNo,
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
    @Override
    public String toString() {
        return "PatientCreateCommand[email=" + email + ", password=***, idCardNo=***"
                + ", firstName=" + firstName + ", lastName=" + lastName
                + ", phoneNumber=" + phoneNumber + ", birthday=" + birthday + "]";
    }
}
