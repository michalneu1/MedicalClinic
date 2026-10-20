package com.medicalclinic.dto.doctor;

import jakarta.validation.constraints.NotNull;

public record SubstitutionCommand(
        @NotNull(message = "substitute id is required")
        Long substituteId
) {
}
