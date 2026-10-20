package com.medicalclinic.dto.facility;

import jakarta.validation.constraints.NotBlank;

public record FacilityCreateCommand(
        @NotBlank(message = "name is required")
        String name,
        @NotBlank(message = "city is required")
        String city,
        String zipCode,
        String street,
        String number
) {
}
