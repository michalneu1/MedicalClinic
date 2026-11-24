package com.medicalclinic.dto.user;

import com.medicalclinic.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.medicalclinic.validation.MaxBytes;

public record UserCreateCommand(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 255, message = "email must be at most 255 characters long")
        String email,
        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters long")
        @MaxBytes(value = 72, message = "password must be at most 72 bytes long")
        String password,
        @NotNull(message = "role is required")
        Role role
) {
    @Override
    public String toString() {
        return "UserCreateCommand[email=" + email + ", password=***, role=" + role + "]";
    }
}
