package com.medicalclinic.service;

import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class PatientQueryCountIntegrationTest {

    @Autowired
    private PatientService patientService;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;
    private final List<Long> createdIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        for (int i = 1; i <= 3; i++) {
            PatientCreateCommand command = new PatientCreateCommand(UUID.randomUUID() + "@example.com",
                    "tajneHaslo1", "QCT00000" + i, "Querycount", "Tester", "50000000" + i,
                    LocalDate.of(1990, 1, i));
            createdIds.add(patientService.create(command).id());
        }
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @AfterEach
    void tearDown() {
        createdIds.forEach(patientService::deleteById);
    }

    @Test
    void findAll_manyPatients_runsSingleQuery() {
        // when
        List<PatientDto> patients = patientService.findAll();

        // then
        assertTrue(patients.size() >= 3);
        assertEquals(1, statistics.getPrepareStatementCount());
    }

    @Test
    void searchByFragment_manyMatches_runsSingleQuery() {
        // when
        List<PatientDto> patients = patientService.searchByFragment("querycount");

        // then
        assertEquals(3, patients.size());
        assertEquals(1, statistics.getPrepareStatementCount());
    }
}
