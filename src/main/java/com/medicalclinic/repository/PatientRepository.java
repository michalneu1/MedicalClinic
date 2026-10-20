package com.medicalclinic.repository;

import com.medicalclinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserEmail(String email);

    boolean existsByUserId(Long userId);

    @Query("""
            select p from Patient p
            where lower(p.firstName) like lower(concat('%', :fragment, '%'))
               or lower(p.lastName)  like lower(concat('%', :fragment, '%'))
            """)
    List<Patient> searchByNameFragment(@Param("fragment") String fragment);
}
