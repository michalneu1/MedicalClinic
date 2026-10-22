package com.medicalclinic.dto.doctor;

import com.medicalclinic.model.Specialization;

import java.util.List;

public record DoctorDto(
        Long id,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Specialization specialization,
        List<String> facilities
) {
}
