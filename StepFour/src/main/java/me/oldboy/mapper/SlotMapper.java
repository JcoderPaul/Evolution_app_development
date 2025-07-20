package me.oldboy.mapper;

/*
Теоретически, сущность Slot не несет какой-то закрытой информации,
например password, как сущность User, и которую мы бы не хотели
отдавать во вне. Однако, в качестве демонстрации работы MapStruct
мы создадим для каждой из наших 4-х основных сущностей по два DTO
класса (EntityReadDto и EntityCreateDto)
*/

import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.models.entity.Slot;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper interface for converting Slot entities to SlotReadDto and vice versa.
 * Интерфейс для преобразования сущности Slot в SlotReadDto / SlotCreateDto в Slot
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SlotMapper {

    SlotMapper INSTANCE = Mappers.getMapper(SlotMapper.class);

    /**
     * Converts a Slot entity to a SlotReadDto.
     * Преобразование сущности Slot в SlotReadDto.
     *
     * @param slot the Slot entity to convert
     * @return the converted SlotReadDto
     */
    SlotReadUpdateDto mapToSlotReadDto(Slot slot);

    /**
     * Converts a SlotCreateDto to a Slot entity.
     * Преобразование SlotCreateDto в сущность Slot.
     *
     * @param slotCreateDeleteDto the DTOs to convert
     * @return the converted Slot entity
     */
    Slot mapToEntity(SlotCreateDeleteDto slotCreateDeleteDto);
}
