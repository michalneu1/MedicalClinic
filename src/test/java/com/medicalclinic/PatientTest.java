package com.medicalclinic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PatientTest {

    @Test
    void lombokGeneratesGettersSettersBuilderAndEquals() {
        Patient fromBuilder = Patient.builder()
                .id(1L)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan.kowalski@example.com")
                .build();

        Patient fromSetters = new Patient();
        fromSetters.setId(1L);
        fromSetters.setFirstName("Jan");
        fromSetters.setLastName("Kowalski");
        fromSetters.setEmail("jan.kowalski@example.com");

        assertEquals("Jan", fromBuilder.getFirstName());
        assertEquals(fromBuilder, fromSetters);
        assertEquals(fromBuilder.hashCode(), fromSetters.hashCode());
        assertEquals("Patient(id=1, firstName=Jan, lastName=Kowalski, email=jan.kowalski@example.com)",
                fromBuilder.toString());

        fromSetters.setEmail("inny@example.com");
        assertNotEquals(fromBuilder, fromSetters);
    }
}
