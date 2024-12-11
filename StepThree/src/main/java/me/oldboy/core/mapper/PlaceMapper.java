package me.oldboy.core.mapper;

/*
Теоретически, сущность Place не несет какой-то закрытой информации,
например password, как сущность User, и которую мы бы не хотели
отдавать во вне. Однако, в качестве демонстрации работы MapStruct
мы создадим для каждой из наших 4-х основных сущностей по два DTO
класса (EntityReadDto и EntityCreateDto)
*/

import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.core.model.database.entity.Place;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper interface for converting Place entities to PlaceReadDto and vice versa.
 * Интерфейс для преобразования сущности Place в PlaceReadDto / PlaceCreateDto в Place
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlaceMapper {

    PlaceMapper INSTANCE = Mappers.getMapper(PlaceMapper.class);

    /**
     * Converts a Place entity to a PlaceReadDto.
     * Преобразование сущности Place в PlaceReadDto.
     *
     * @param place the Place entity to convert
     * @return the converted PlaceReadDto
     */
    PlaceReadUpdateDto mapToPlaceReadDto(Place place);

    /**
     * Converts a PlaceCreateDto to a Place entity.
     * Преобразование PlaceCreateDto в сущность Place.
     *
     * @param placeCreateDto the DTOs to convert
     * @return the converted Place entity
     */
    Place mapToEntity(PlaceCreateDeleteDto placeCreateDto);
}
