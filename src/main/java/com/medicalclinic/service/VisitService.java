package com.medicalclinic.service;

import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.DoctorNotFoundException;
import com.medicalclinic.exception.InvalidVisitException;
import com.medicalclinic.exception.PatientNotFoundException;
import com.medicalclinic.exception.VisitAlreadyBookedException;
import com.medicalclinic.exception.VisitNotFoundException;
import com.medicalclinic.exception.VisitOverlapsException;
import com.medicalclinic.mapper.VisitMapper;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Patient;
import com.medicalclinic.model.Visit;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.PatientRepository;
import com.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper visitMapper;
    private final Clock clock;

    @Transactional
    public VisitDto create(VisitCreateCommand command) {
        validateTime(command.startsAt(), command.endsAt());
        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));
        if (visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(
                command.doctorId(), command.endsAt(), command.startsAt())) {
            throw new VisitOverlapsException();
        }
        Visit visit = visitMapper.toEntity(command);
        visit.setDoctor(doctor);
        return visitMapper.toDto(visitRepository.save(visit));
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
    public List<VisitDto> findByDoctorId(Long doctorId) {
        return visitRepository
                .findByDoctorId(doctorId)
                .stream()
                .map(visitMapper::toDto).toList();
    }
}
