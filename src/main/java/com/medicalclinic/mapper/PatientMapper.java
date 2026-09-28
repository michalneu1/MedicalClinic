package com.medicalclinic.mapper;

import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.PatientUpdateCommand;
import com.medicalclinic.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PatientMapper {

    PatientDto toDto(Patient patient);

    @Mapping(target = "id", ignore = true)
    Patient toEntity(PatientCreateCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idCardNo", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(PatientUpdateCommand command, @MappingTarget Patient patient);
}
