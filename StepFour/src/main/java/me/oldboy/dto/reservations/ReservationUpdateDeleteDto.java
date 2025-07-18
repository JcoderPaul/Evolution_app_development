package me.oldboy.dto.reservations;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ReservationUpdateDeleteDto(@NotNull(message = "Reservation ID can not be blank")
                                         @PositiveOrZero
                                         Long reservationId,
                                         @NotNull(message = "Reservation date can not be blank or null")
                                         @FutureOrPresent
                                         LocalDate reservationDate,
                                         @NotNull(message = "User ID can not be blank")
                                         @PositiveOrZero
                                         Long userId,
                                         @NotNull(message = "Place ID can not be blank")
                                         @PositiveOrZero
                                         Long placeId,
                                         @NotNull(message = "Slot ID date can not be blank")
                                         @PositiveOrZero
                                         Long slotId) {
}
