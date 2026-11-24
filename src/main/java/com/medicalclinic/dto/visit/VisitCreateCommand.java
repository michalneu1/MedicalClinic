package com.medicalclinic.dto.visit;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record VisitCreateCommand(
        @NotNull(message = "doctor id is required")
        Long doctorId,
        @NotNull(message = "start time is required")
        LocalDateTime startsAt,
        @NotNull(message = "end time is required")
        LocalDateTime endsAt
) {
}
