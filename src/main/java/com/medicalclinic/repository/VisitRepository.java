package com.medicalclinic.repository;

import com.medicalclinic.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    boolean existsByDoctorId(Long doctorId);

    boolean existsByPatientId(Long patientId);

    List<Visit> findByDoctorId(Long doctorId);

    List<Visit> findByDoctorIdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(Long doctorId, LocalDateTime from);

    List<Visit> findByPatientIdOrderByStartsAtAscIdAsc(Long patientId);

    boolean existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(Long doctorId,
                                                            LocalDateTime endsAt,
                                                            LocalDateTime startsAt);

    @Query("""
            select count(v) from Visit v
            where v.doctor.id = :doctorId
              and v.patient is null
              and v.startsAt >= :from
              and v.startsAt < :to
            """)
    long countFreeSlots(@Param("doctorId") Long doctorId,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to);
}
