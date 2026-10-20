package com.medicalclinic.service;

import com.medicalclinic.dto.doctor.DoctorCreateCommand;
import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.exception.DoctorAlreadyExistsException;
import com.medicalclinic.exception.DoctorHasVisitsException;
import com.medicalclinic.exception.DoctorNotFoundException;
import com.medicalclinic.exception.FacilityNotFoundException;
import com.medicalclinic.exception.UserNotFoundException;
import com.medicalclinic.mapper.DoctorMapper;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Facility;
import com.medicalclinic.model.User;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.FacilityRepository;
import com.medicalclinic.repository.UserRepository;
import com.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;
    private final FacilityRepository facilityRepository;
    private final DoctorMapper doctorMapper;

    public List<DoctorDto> findAll() {
        return doctorRepository.findAll().stream()
                .map(doctorMapper::toDto)
                .toList();
    }

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
        return doctorMapper.toDto(doctorRepository.save(doctor));
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
    }

    public DoctorDto assignFacility(Long id, Long facilityId) {
        Doctor doctor = findDoctor(id);
        Facility facility = findFacility(facilityId);
        doctor.addFacility(facility);
        return doctorMapper.toDto(doctorRepository.save(doctor));
    }

    public DoctorDto unassignFacility(Long id, Long facilityId) {
        Doctor doctor = findDoctor(id);
        Facility facility = findFacility(facilityId);
        doctor.removeFacility(facility);
        return doctorMapper.toDto(doctorRepository.save(doctor));
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
