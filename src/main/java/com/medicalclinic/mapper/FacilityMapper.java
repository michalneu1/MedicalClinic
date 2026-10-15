package com.medicalclinic.mapper;

import com.medicalclinic.dto.facility.FacilityCreateCommand;
import com.medicalclinic.dto.facility.FacilityDto;
import com.medicalclinic.model.Facility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FacilityMapper {

    FacilityDto toDto(Facility facility);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    Facility toEntity(FacilityCreateCommand command);
}
