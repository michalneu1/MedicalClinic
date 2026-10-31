package com.medicalclinic.dto.auth;

public record TokenDto(String token, String tokenType, long expiresIn) {
}
