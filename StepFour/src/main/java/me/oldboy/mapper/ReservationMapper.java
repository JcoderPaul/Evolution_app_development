package me.oldboy.mapper;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.models.entity.Place;
import me.oldboy.models.entity.Reservation;
import me.oldboy.models.entity.Slot;
import me.oldboy.models.entity.User;
import me.oldboy.repository.PlaceRepository;
import me.oldboy.repository.SlotRepository;
import me.oldboy.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper interface for converting Reservation entities to ReservationReadDto and vice versa.
 * Интерфейс для преобразования сущности Reservation в ReservationReadDto / ReservationCreateDto в Reservation
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@AllArgsConstructor
@NoArgsConstructor
public abstract class ReservationMapper {
    @Setter
    UserRepository userRepository;
    @Setter
    SlotRepository slotRepository;
    @Setter
    PlaceRepository placeRepository;
    public static final ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);

    /**
     * Converts a Reservation entity to a ReservationReadDto.
     * Преобразование сущности Reservation в ReservationReadDto.
     *
     * @param reservation the Reservation entity to convert
     * @return the converted ReservationReadDto
     */
    @Mapping(target = "userId", source = "reservation.user.userId")
    @Mapping(target = "slotId", source = "reservation.slot.slotId")
    @Mapping(target = "placeId", source = "reservation.place.placeId")
    public abstract ReservationReadDto mapToReservationReadDto(Reservation reservation);

    /**
     * Converts a ReservationCreateDto to a Reservation entity.
     * Преобразование ReservationCreateDto в сущность Reservation.
     *
     * @param reservationCreateDto the DTOs to convert
     * @return the converted Reservation entity
     */
    @Mapping(target = "user", source = "reservationCreateDto.userId", qualifiedByName = "getUser")
    @Mapping(target = "slot", source = "reservationCreateDto.slotId", qualifiedByName = "getSlot")
    @Mapping(target = "place", source = "reservationCreateDto.placeId", qualifiedByName = "getPlace")
    public abstract Reservation mapToEntityFromCreateDto(ReservationCreateDto reservationCreateDto);

    /**
     * Converts a ReservationUpdateDeleteDto to a Reservation entity.
     * Преобразование ReservationUpdateDeleteDto в сущность Reservation.
     *
     * @param reservationUpdateDeleteDto the DTOs to convert
     * @return the converted Reservation entity
     */
    @Mapping(target = "reservationId", source = "reservationUpdateDeleteDto.reservationId")
    @Mapping(target = "reservationDate", source = "reservationUpdateDeleteDto.reservationDate")
    @Mapping(target = "user", source = "reservationUpdateDeleteDto.userId", qualifiedByName = "getUser")
    @Mapping(target = "slot", source = "reservationUpdateDeleteDto.slotId", qualifiedByName = "getSlot")
    @Mapping(target = "place", source = "reservationUpdateDeleteDto.placeId", qualifiedByName = "getPlace")
    public abstract Reservation mapToEntityFromUpdateDeleteDto(ReservationUpdateDeleteDto reservationUpdateDeleteDto);

    @Mapping(target = "reservationId", source = "reservationReadDto.reservationId")
    @Mapping(target = "reservationDate", source = "reservationReadDto.reservationDate")
    @Mapping(target = "user", source = "reservationReadDto.userId", qualifiedByName = "getUser")
    @Mapping(target = "slot", source = "reservationReadDto.slotId", qualifiedByName = "getSlot")
    @Mapping(target = "place", source = "reservationReadDto.placeId", qualifiedByName = "getPlace")
    public abstract Reservation mapToEntityFromReadDto(ReservationReadDto reservationReadDto);

    @Named("getUser")
    User getUser(Long userId) {
        return userRepository.findById(userId).get();
    }

    @Named("getSlot")
    Slot getSlot(Long slotId) {
        return slotRepository.findById(slotId).get();
    }

    @Named("getPlace")
    Place getPace(Long placeId) {
        return placeRepository.findById(placeId).get();
    }
}