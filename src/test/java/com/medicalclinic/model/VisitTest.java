package com.medicalclinic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitTest {

    @Test
    void isFree_noPatient_returnsTrue() {
        // given
        Visit visit = Visit.builder().build();

        // when + then
        assertTrue(visit.isFree());
    }

    @Test
    void isFree_patientAssigned_returnsFalse() {
        // given
        Visit visit = Visit.builder().patient(Patient.builder().id(2L).build()).build();

        // when + then
        assertFalse(visit.isFree());
    }
}
