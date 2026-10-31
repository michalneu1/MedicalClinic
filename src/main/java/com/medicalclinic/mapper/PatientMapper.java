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

    @Mapping(target = "email", source = "user.email")
    PatientDto toDto(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Patient toEntity(PatientCreateCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idCardNo", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(PatientUpdateCommand command, @MappingTarget Patient patient);
}
