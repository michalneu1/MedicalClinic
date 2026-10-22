package com.medicalclinic.dto.user;

import com.medicalclinic.model.Role;

public record UserDto(
        Long id,
        String email,
        Role role
) {
}
