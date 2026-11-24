package com.medicalclinic.service;

import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.exception.VisitOverlapsException;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Role;
import com.medicalclinic.model.Specialization;
import com.medicalclinic.model.User;
import com.medicalclinic.model.Visit;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.UserRepository;
import com.medicalclinic.repository.VisitRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class VisitConcurrentCreateIntegrationTest {

    private static final int REQUESTS = 15;

    @Autowired
    private VisitService visitService;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private UserRepository userRepository;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        User user = User.builder().email(UUID.randomUUID() + "@example.com").password("x").role(Role.DOCTOR).build();
        doctor = doctorRepository.save(Doctor.builder().user(user).firstName("Rownolegly").lastName("Tester")
                .specialization(Specialization.CARDIOLOGIST).build());
    }

    @AfterEach
    void tearDown() {
        visitRepository.deleteAll(visitRepository.findByDoctorId(doctor.getId()));
        doctorRepository.deleteById(doctor.getId());
        userRepository.deleteById(doctor.getUser().getId());
    }

    @Test
    void create_manyRequestsForSameSlot_savesExactlyOneVisit() throws Exception {
        // given
        LocalDateTime start = LocalDate.now().plusYears(1).atTime(10, 0);
        VisitCreateCommand command = new VisitCreateCommand(doctor.getId(), start, start.plusMinutes(30));
        ExecutorService executor = Executors.newFixedThreadPool(REQUESTS);
        CountDownLatch ready = new CountDownLatch(REQUESTS);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        // when
        for (int i = 0; i < REQUESTS; i++) {
            results.add(executor.submit(() -> {
                ready.countDown();
                go.await();
                try {
                    visitService.create(command);
                    return true;
                } catch (VisitOverlapsException exception) {
                    return false;
                }
            }));
        }
        ready.await(10, TimeUnit.SECONDS);
        go.countDown();
        int created = 0;
        for (Future<Boolean> result : results) {
            if (result.get(30, TimeUnit.SECONDS)) {
                created++;
            }
        }
        executor.shutdown();

        // then
        List<Visit> visits = visitRepository.findByDoctorId(doctor.getId());
        assertEquals(1, created);
        assertEquals(1, visits.size());
    }
}
