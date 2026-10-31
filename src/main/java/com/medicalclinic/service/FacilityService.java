package com.medicalclinic.service;

import com.medicalclinic.dto.facility.FacilityCreateCommand;
import com.medicalclinic.dto.facility.FacilityDto;
import com.medicalclinic.exception.FacilityAlreadyExistsException;
import com.medicalclinic.exception.FacilityNotFoundException;
import com.medicalclinic.mapper.FacilityMapper;
import com.medicalclinic.model.Facility;
import com.medicalclinic.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;

    @Transactional(readOnly = true)
    public List<FacilityDto> findAll() {
        return facilityRepository.findAll().stream()
                .map(facilityMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacilityDto findById(Long id) {
        return facilityRepository.findById(id)
                .map(facilityMapper::toDto)
                .orElseThrow(() -> new FacilityNotFoundException(id));
    }

    @Transactional
    public FacilityDto create(FacilityCreateCommand command) {
        if (facilityRepository.existsByName(command.name())) {
            throw new FacilityAlreadyExistsException(command.name());
        }
        Facility saved = facilityRepository.save(facilityMapper.toEntity(command));
        log.info("created facility {}", saved.getId());
        return facilityMapper.toDto(saved);
    }
}
