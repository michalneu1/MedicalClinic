package com.medicalclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.medicalclinic.validation.MaxBytes;

public record EditPasswordCommand(
        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters long")
        @MaxBytes(value = 72, message = "password must be at most 72 bytes long")
        String password) {

    @Override
    public String toString() {
        return "EditPasswordCommand[password=***]";
    }
}
