package me.oldboy.dto.reservations;

import lombok.Builder;

import java.time.LocalDate;

/**
 * A record representing reservation read info.
 */
@Builder
public record ReservationReadDto(Long reservationId,
                                 LocalDate reservationDate,
                                 Long userId,
                                 Long placeId,
                                 Long slotId) {
}
