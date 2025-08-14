package me.oldboy.dto.reservations;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Class representing create reservation info.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateDto {

        /**
         * Reservation date
         */
        @NotNull(message = "Reservation date can not be blank or null")
        @FutureOrPresent(message = "Reservation date must be a date in the present or in the future")
        @Getter
        private LocalDate reservationDate;

        /**
         * User id for whom the reservation is made
         */
        @NotNull(message = "User ID can not be blank or negative")
        @PositiveOrZero(message = "User ID must be greater than or equal to 0")
        @Setter
        @Getter
        private Long userId;

        /**
         * Place id that was booked
         */
        @NotNull(message = "Place ID can not be blank or negative")
        @PositiveOrZero(message = "Place ID must be greater than or equal to 0")
        @Getter
        private Long placeId;

        /**
         * Slot id that was booked
         */
        @NotNull(message = "Slot ID date can not be blank or negative")
        @PositiveOrZero(message = "Slot ID must be greater than or equal to 0")
        @Getter
        private Long slotId;

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ReservationCreateDto that = (ReservationCreateDto) o;
                return Objects.equals(reservationDate, that.reservationDate) && Objects.equals(userId, that.userId) && Objects.equals(placeId, that.placeId) && Objects.equals(slotId, that.slotId);
        }

        @Override
        public int hashCode() {
                return Objects.hash(reservationDate, userId, placeId, slotId);
        }

        @Override
        public String toString() {
                return "ReservationCreateDto [" +
                        "reservationDate =" + reservationDate +
                        ", userId = " + userId +
                        ", placeId = " + placeId +
                        ", slotId = " + slotId +
                        ']';
        }
}