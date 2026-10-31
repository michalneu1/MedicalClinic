package com.medicalclinic.repository;

import com.medicalclinic.model.Doctor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = {"user", "facilities"})
    List<Doctor> findAll();
}
