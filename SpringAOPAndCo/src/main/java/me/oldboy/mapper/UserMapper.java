package me.oldboy.mapper;

import me.oldboy.dto.UserCreateDto;
import me.oldboy.dto.UserReadDto;
import me.oldboy.entity.User;
import me.oldboy.entity.options.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserReadDto mapToUserReadDto(User user);

    @Mapping(target = "role", qualifiedByName = "roleConvert", source = "role")
    User mapToEntity(UserCreateDto userCreateDto);

    @Named("roleConvert")
    static Role roleConvert(String role) {
        return Role.valueOf(role);
    }
}
