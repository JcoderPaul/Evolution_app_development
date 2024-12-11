package me.oldboy.core.dto.reservations;

import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateDto {
        @NotNull(message = "Reservation date can not be blank or null")
        @FutureOrPresent
        @Getter
        private LocalDate reservationDate;

        @NotNull(message = "User ID can not be blank")
        @PositiveOrZero
        @Setter
        @Getter
        private Long userId;

        @NotNull(message = "Place ID can not be blank")
        @PositiveOrZero
        @Getter
        private Long placeId;

        @NotNull(message = "Slot ID date can not be blank")
        @PositiveOrZero
        @Getter
        private Long slotId;

        @Override
        public String toString() {
                return "ReservationCreate {" +
                        "reservationDate: " + reservationDate +
                        ", userId = " + userId +
                        ", placeId = " + placeId +
                        ", slotId = " + slotId +
                        '}';
        }
}