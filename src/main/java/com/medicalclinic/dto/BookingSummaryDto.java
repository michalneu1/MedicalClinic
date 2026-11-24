package com.medicalclinic.dto;

import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.dto.visit.VisitDto;

public record BookingSummaryDto(
        VisitDto visit,
        PatientDto patient,
        DoctorDto doctor,
        long freeSlotsThatDay
) {
}
