package com.medicalclinic.repository;

import com.medicalclinic.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    boolean existsByDoctorId(Long doctorId);

    List<Visit> findByDoctorId(Long doctorId);

    List<Visit> findByDoctorIdOrderByStartsAtAsc(Long doctorId);

    boolean existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(Long doctorId,
                                                            LocalDateTime endsAt,
                                                            LocalDateTime startsAt);
}
