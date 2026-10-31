package com.medicalclinic.service;

import com.medicalclinic.dto.doctor.DoctorCreateCommand;
import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.dto.doctor.SubstitutionCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.DoctorAlreadyExistsException;
import com.medicalclinic.exception.DoctorHasVisitsException;
import com.medicalclinic.exception.DoctorNotFoundException;
import com.medicalclinic.exception.FacilityNotFoundException;
import com.medicalclinic.exception.UserNotFoundException;
import com.medicalclinic.exception.VisitOverlapsException;
import com.medicalclinic.mapper.DoctorMapper;
import com.medicalclinic.mapper.VisitMapper;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Facility;
import com.medicalclinic.model.User;
import com.medicalclinic.model.Visit;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.FacilityRepository;
import com.medicalclinic.repository.UserRepository;
import com.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;
    private final FacilityRepository facilityRepository;
    private final DoctorMapper doctorMapper;
    private final VisitMapper visitMapper;

    @Transactional(readOnly = true)
    public List<DoctorDto> findAll() {
        return doctorRepository.findAll().stream()
                .map(doctorMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorDto findById(Long id) {
        return doctorRepository.findById(id)
                .map(doctorMapper::toDto)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    @Transactional
    public DoctorDto create(DoctorCreateCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (doctorRepository.existsByUserId(command.userId())) {
            throw new DoctorAlreadyExistsException(command.userId());
        }
        Doctor doctor = doctorMapper.toEntity(command);
        doctor.setUser(user);
        Doctor saved = doctorRepository.save(doctor);
        log.info("created doctor {} for user {}", saved.getId(), user.getId());
        return doctorMapper.toDto(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new DoctorNotFoundException(id);
        }
        if (visitRepository.existsByDoctorId(id)) {
            throw new DoctorHasVisitsException(id);
        }
        doctorRepository.deleteById(id);
        log.info("deleted doctor {}", id);
    }

    @Transactional
    public List<VisitDto> substitute(Long id, SubstitutionCommand command) {
        findDoctor(id);
        Doctor substitute = findDoctor(command.substituteId());
        List<Visit> visits = visitRepository.findByDoctorIdOrderByStartsAtAsc(id);
        for (Visit visit : visits) {
            if (visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(
                    substitute.getId(), visit.getEndsAt(), visit.getStartsAt())) {
                throw new VisitOverlapsException();
            }
            visit.setDoctor(substitute);
        }
        log.info("substitution: {} visits moved from doctor {} to doctor {}", visits.size(), id, substitute.getId());
        return visits.stream()
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional
    public DoctorDto assignFacility(Long id, Long facilityId) {
        Doctor doctor = findDoctor(id);
        Facility facility = findFacility(facilityId);
        doctor.addFacility(facility);
        log.info("assigned doctor {} to facility {}", id, facilityId);
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto unassignFacility(Long id, Long facilityId) {
        Doctor doctor = findDoctor(id);
        Facility facility = findFacility(facilityId);
        doctor.removeFacility(facility);
        log.info("unassigned doctor {} from facility {}", id, facilityId);
        return doctorMapper.toDto(doctor);
    }

    private Doctor findDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    private Facility findFacility(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new FacilityNotFoundException(id));
    }
}
