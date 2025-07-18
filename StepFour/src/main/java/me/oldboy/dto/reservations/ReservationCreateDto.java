package me.oldboy.dto.reservations;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateDto {
        @NotNull(message = "Reservation date can not be blank or null")
        @FutureOrPresent(message = "Reservation date must be a date in the present or in the future")
        @Getter
        private LocalDate reservationDate;

        @NotNull(message = "User ID can not be blank or negative")
        @PositiveOrZero(message = "ID must be greater than or equal to 0")
        @Setter
        @Getter
        private Long userId;

        @NotNull(message = "Place ID can not be blank or negative")
        @PositiveOrZero(message = "ID must be greater than or equal to 0")
        @Getter
        private Long placeId;

        @NotNull(message = "Slot ID date can not be blank or negative")
        @PositiveOrZero(message = "ID must be greater than or equal to 0")
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