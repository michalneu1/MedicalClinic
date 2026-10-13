package com.medicalclinic.mapper;

import com.medicalclinic.dto.user.UserCreateCommand;
import com.medicalclinic.dto.user.UserDto;
import com.medicalclinic.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserCreateCommand command);
}
