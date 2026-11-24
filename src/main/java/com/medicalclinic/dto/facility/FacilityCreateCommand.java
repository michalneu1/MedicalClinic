package com.medicalclinic.dto.facility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FacilityCreateCommand(
        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must be at most 255 characters long")
        String name,
        @NotBlank(message = "city is required")
        @Size(max = 255, message = "city must be at most 255 characters long")
        String city,
        @Size(max = 255, message = "zip code must be at most 255 characters long")
        String zipCode,
        @Size(max = 255, message = "street must be at most 255 characters long")
        String street,
        @Size(max = 255, message = "number must be at most 255 characters long")
        String number
) {
}
