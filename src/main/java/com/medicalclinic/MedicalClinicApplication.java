package com.medicalclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MedicalClinicApplication {
    public static void main(String[] args) {
        Patient patient = Patient.builder()
                .id(1)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan.kowalski@example.com")
                .build();
        System.out.println(patient);
        SpringApplication.run(MedicalClinicApplication.class, args);

    }
}
