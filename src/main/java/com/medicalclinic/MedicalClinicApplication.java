package com.medicalclinic;

public class MedicalClinicApplication {
    public static void main(String[] args) {
        Patient patient = Patient.builder()
                .id(1)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan.kowalski@example.com")
                .build();
        System.out.println(patient);
    }
}
