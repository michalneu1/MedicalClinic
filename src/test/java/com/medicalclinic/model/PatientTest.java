package com.medicalclinic.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PatientTest {

    @Test
    void hashSet_idAssignedAfterAdd_stillContainsPatient() {
        // given
        Patient patient = Patient.builder().firstName("Jan").build();
        Set<Patient> patients = new HashSet<>();
        patients.add(patient);

        // when
        patient.setId(42L);

        // then
        assertTrue(patients.contains(patient));
    }

    @Test
    void equals_sameId_returnsTrue() {
        // given
        Patient first = Patient.builder().id(42L).build();
        Patient second = Patient.builder().id(42L).build();

        // when
        boolean result = first.equals(second);

        // then
        assertTrue(result);
    }

    @Test
    void equals_bothIdsNull_returnsFalse() {
        // given
        Patient first = Patient.builder().firstName("Jan").build();
        Patient second = Patient.builder().firstName("Jan").build();

        // when
        boolean result = first.equals(second);

        // then
        assertFalse(result);
    }
}
