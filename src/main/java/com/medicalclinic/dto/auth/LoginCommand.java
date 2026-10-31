package com.medicalclinic.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank(message = "email is required")
        String email,
        @NotBlank(message = "password is required")
        String password
) {
}
