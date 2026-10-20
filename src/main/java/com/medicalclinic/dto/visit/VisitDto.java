package com.medicalclinic.dto.visit;

import java.time.LocalDateTime;

public record VisitDto(
        Long id,
        Long doctorId,
        Long patientId,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}
