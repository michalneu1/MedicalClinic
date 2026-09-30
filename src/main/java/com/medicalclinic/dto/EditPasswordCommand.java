package com.medicalclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditPasswordCommand(
        @NotBlank
        @Size(min = 8)
        String password) {
}
