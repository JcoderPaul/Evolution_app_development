package me.oldboy.dto.reservations;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ReservationUpdateDeleteDto(@NotNull(message = "Reservation ID can not be blank")
                                         @PositiveOrZero(message = "Reservation ID must be greater than or equal to 0")
                                         Long reservationId,
                                         @NotNull(message = "Reservation date can not be blank or null")
                                         @FutureOrPresent
                                         LocalDate reservationDate,
                                         @NotNull(message = "User ID can not be blank")
                                         @PositiveOrZero(message = "User ID must be greater than or equal to 0")
                                         Long userId,
                                         @NotNull(message = "Place ID can not be blank")
                                         @PositiveOrZero(message = "Place ID must be greater than or equal to 0")
                                         Long placeId,
                                         @NotNull(message = "Slot ID date can not be blank")
                                         @PositiveOrZero(message = "Slot ID must be greater than or equal to 0")
                                         Long slotId) {
}
