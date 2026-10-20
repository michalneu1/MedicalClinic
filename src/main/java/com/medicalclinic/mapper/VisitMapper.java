package com.medicalclinic.mapper;

import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.model.Visit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VisitMapper {

    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "patientId", source = "patient.id")
    VisitDto toDto(Visit visit);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "patient", ignore = true)
    Visit toEntity(VisitCreateCommand command);
}
