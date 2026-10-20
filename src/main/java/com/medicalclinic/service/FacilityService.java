package com.medicalclinic.service;

import com.medicalclinic.dto.facility.FacilityCreateCommand;
import com.medicalclinic.dto.facility.FacilityDto;
import com.medicalclinic.exception.FacilityAlreadyExistsException;
import com.medicalclinic.exception.FacilityNotFoundException;
import com.medicalclinic.mapper.FacilityMapper;
import com.medicalclinic.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;

    public List<FacilityDto> findAll() {
        return facilityRepository.findAll().stream()
                .map(facilityMapper::toDto)
                .toList();
    }

    public FacilityDto findById(Long id) {
        return facilityRepository.findById(id)
                .map(facilityMapper::toDto)
                .orElseThrow(() -> new FacilityNotFoundException(id));
    }

    public FacilityDto create(FacilityCreateCommand command) {
        if (facilityRepository.existsByName(command.name())) {
            throw new FacilityAlreadyExistsException(command.name());
        }
        return facilityMapper.toDto(facilityRepository.save(facilityMapper.toEntity(command)));
    }
}
