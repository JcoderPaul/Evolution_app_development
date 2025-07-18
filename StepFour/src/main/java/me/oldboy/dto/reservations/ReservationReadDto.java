package me.oldboy.dto.reservations;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ReservationReadDto(Long reservationId,
                                 LocalDate reservationDate,
                                 Long userId,
                                 Long placeId,
                                 Long slotId) {
}
