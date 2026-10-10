package com.medicalclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditPasswordCommand(
        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters long")
        String password) {
}
