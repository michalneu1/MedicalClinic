package com.medicalclinic.dto.facility;

public record FacilityDto(
        Long id,
        String name,
        String city,
        String zipCode,
        String street,
        String number
) {
}
