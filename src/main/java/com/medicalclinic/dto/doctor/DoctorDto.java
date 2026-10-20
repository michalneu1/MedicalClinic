package com.medicalclinic.dto.doctor;

import com.medicalclinic.model.Specialization;

public record DoctorDto(
        Long id,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Specialization specialization
) {
}
