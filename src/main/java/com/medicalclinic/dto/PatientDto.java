package com.medicalclinic.dto;

import java.time.LocalDate;

public record PatientDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate birthday
) {
}
