package com.medicalclinic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DoctorTest {

    @Test
    void removeFacility_idAssignedAfterAdd_removesFacility() {
        // given
        Doctor doctor = Doctor.builder().firstName("Adam").build();
        Facility facility = Facility.builder().name("Przychodnia Centrum").build();
        doctor.addFacility(facility);
        facility.setId(7L);

        // when
        doctor.removeFacility(facility);

        // then
        assertTrue(doctor.getFacilities().isEmpty());
    }
}
