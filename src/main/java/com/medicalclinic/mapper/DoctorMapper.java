package com.medicalclinic.mapper;

import com.medicalclinic.dto.doctor.DoctorCreateCommand;
import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.model.Doctor;
import com.medicalclinic.model.Facility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DoctorMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    DoctorDto toDto(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "visits", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    Doctor toEntity(DoctorCreateCommand command);

    default List<String> facilityNames(Set<Facility> facilities) {
        return facilities.stream()
                .map(Facility::getName)
                .sorted()
                .toList();
    }
}
