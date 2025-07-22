package me.oldboy.mapper;

import me.oldboy.dto.places.PlaceCreateDeleteDto;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.models.entity.Place;
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
