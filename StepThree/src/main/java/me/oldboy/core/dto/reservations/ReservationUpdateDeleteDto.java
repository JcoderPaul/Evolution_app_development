package me.oldboy.core.dto.reservations;

import lombok.Builder;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/*
Данный класс будет использоваться для удаления и обновления данных о резервировании.
И тут возникает ситуация при которой мы можем разделить объекты по специализации,
только для обновления и только для удаления. Однако, чтобы удалить запись о брони из БД
достаточно ее ID, он же понадобится для частичного обновления записи по этому же ID.
Поэтому объединяем, но в процессе будет задействована только нужная часть полей.
* */

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
    @Override
    public String toString() {
        return "ReservationUpdateDeleteDto {" +
                "reservationId: " + reservationId +
                ", reservationDate = " + reservationDate +
                ", userId = " + userId +
                ", placeId = " + placeId +
                ", slotId = " + slotId +
                '}';
    }
}
