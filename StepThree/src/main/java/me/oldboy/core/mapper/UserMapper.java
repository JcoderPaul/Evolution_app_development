package me.oldboy.core.mapper;

import me.oldboy.core.dto.users.UserCreateDto;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.model.database.entity.options.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper interface for converting User entities to UserReadDto and vice versa.
 * Интерфейс для преобразования сущности User в UserReadDto / UserCreateDto в User
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Converts a User entity to a UserReadDto.
     * Преобразование сущности User в UserReadDto.
     *
     * @param user the User entity to convert
     * @return the converted UserReadDto
     */
    UserReadDto mapToUserReadDto(User user);

    /**
     * Converts a UserCreateDto to a User entity.
     * Преобразование UserCreateDto в сущность User.
     *
     * @param userCreateDto the DTOs to convert
     * @return the converted User entity
     */
    @Mapping(target = "role", qualifiedByName = "roleConvert", source = "role")
    User mapToEntity(UserCreateDto userCreateDto);

    /*
    При разработке UserCreateDto мы не стали заморачиваться с валидацией Role, а сделали его просто String,
    что проще валидировать стандартными средствами. Но преобразовывать UserCreateDto в User нам все же надо,
    посему допишем метод, который преобразует строковое значение поле в его Role эквивалент. Хотя можно было
    поступить как в методе *.update() класса UserService для сведения типов (см. ниже) и тогда методы:
    *.mapToEntity() и *.roleConvert() нам бы вовсе не понадобились.

    Пример грубого сведения без использования маппера в методе *.create() класса UserService:
    User userTransferToRepository = User.builder()
                                        .userName(userCreateDto.userName())
                                        .password(userCreateDto.password())
                                        .role(userCreateDto.role().transform(s -> Role.valueOf(s.toUpperCase())))
                                        .build();
    */
    @Named("roleConvert")
    static Role roleConvert(String role) {
        return Role.valueOf(role);
    }
}
