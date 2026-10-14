package com.medicalclinic.service;

import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.DoctorNotFoundException;
import com.medicalclinic.mapper.VisitMapper;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Visit;
import com.medicalclinic.repository.DoctorRepository;
import com.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final VisitMapper visitMapper;

    @Transactional
    public VisitDto create(VisitCreateCommand command) {
        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));
        Visit visit = visitMapper.toEntity(command);
        visit.setDoctor(doctor);
        return visitMapper.toDto(visitRepository.save(visit));
    }
    public List<VisitDto> findByDoctorId(Long doctorId)
    {
        return visitRepository
                .findByDoctorId(doctorId)
                .stream()
                .map(visitMapper::toDto).toList();
    }
}
