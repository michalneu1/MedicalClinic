package com.medicalclinic.service;

import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.*;
import com.medicalclinic.mapper.VisitMapper;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Patient;
import com.medicalclinic.model.Visit;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.PatientRepository;
import com.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper visitMapper;
    private final Clock clock;
    private static final List<String> SORTABLE_FIELDS = List.of("startsAt", "endsAt");

    @Transactional
    public VisitDto create(VisitCreateCommand command) {
        validateTime(command.startsAt(), command.endsAt());
        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));
        log.debug("checking overlapping visits of doctor {} between {} and {}",
                command.doctorId(), command.startsAt(), command.endsAt());
        if (visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(
                command.doctorId(), command.endsAt(), command.startsAt())) {
            throw new VisitOverlapsException();
        }
        Visit visit = visitMapper.toEntity(command);
        visit.setDoctor(doctor);
        Visit saved = visitRepository.save(visit);
        log.info("created visit {} for doctor {} from {} to {}",
                saved.getId(), doctor.getId(), saved.getStartsAt(), saved.getEndsAt());
        return visitMapper.toDto(saved);
    }

    @Transactional
    public VisitDto book(Long visitId, Long patientId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        if (visit.getStartsAt().isBefore(LocalDateTime.now(clock))) {
            throw new InvalidVisitException("Visit in the past cannot be booked");
        }
        if (visit.getPatient() != null) {
            throw new VisitAlreadyBookedException(visitId);
        }
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
        visit.setPatient(patient);
        log.info("booked visit {} for patient {}", visitId, patientId);
        return visitMapper.toDto(visit);
    }

    private void validateTime(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (startsAt.isBefore(LocalDateTime.now(clock))) {
            throw new InvalidVisitException("Visit cannot start in the past");
        }
        if (startsAt.getMinute() % 15 != 0 || startsAt.getSecond() != 0) {
            throw new InvalidVisitException("Visit must start on a full quarter hour (e.g. 10:00, 10:15)");
        }
        Duration duration = Duration.between(startsAt, endsAt);
        if (duration.isNegative() || duration.isZero() || duration.toSeconds() % 900 != 0) {
            throw new InvalidVisitException("Visit length must be a positive multiple of 15 minutes");
        }
    }

    @Transactional(readOnly = true)
    public Page<Visit> findAll(Pageable pageable) {
        validateSort(pageable.getSort());
        return visitRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public long countFreeSlots(Long doctorId, LocalDate day) {
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.plusDays(1).atStartOfDay();
        long freeSlots = visitRepository.countFreeSlots(doctorId, from, to);
        log.debug("free slots of doctor {} on {}: {}", doctorId, day, freeSlots);
        return freeSlots;
    }

    @Transactional(readOnly = true)
    public List<VisitDto> findByDoctorId(Long doctorId) {
        return visitRepository
                .findByDoctorId(doctorId)
                .stream()
                .map(visitMapper::toDto).toList();
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new InvalidSortPropertyException(order.getProperty(), SORTABLE_FIELDS);
            }
        }
    }
}
