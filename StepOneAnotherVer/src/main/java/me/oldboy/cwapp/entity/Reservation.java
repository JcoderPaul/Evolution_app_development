package me.oldboy.cwapp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.oldboy.cwapp.exception.ReservationUnitException;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private Long reservationId;
    private LocalDate reservationDate;
    private Long reservationPlaceId;
    private Long reservationUserId;
    private LocalTime startTime;
    private LocalTime finishTime;

    public Reservation(LocalDate reservationDate,
                       Long reservationPlaceId,
                       Long reservationUserId,
                       LocalTime startTime,
                       LocalTime finishTime) {
        this.reservationDate = reservationDate;
        this.reservationPlaceId = reservationPlaceId;
        this.reservationUserId = reservationUserId;
        this.startTime = startTime;
        if(startTime.isBefore(finishTime)){
        this.finishTime = finishTime;
        } else {
            throw new ReservationUnitException("Время начала резервирования всегда " +
                                               "раньше времени окончания!");
        }
    }

    public void setStartTime(LocalTime startTime) {
        if(this.getFinishTime().isAfter(startTime)){
            this.startTime = startTime;
        } else {
            throw new ReservationUnitException("Время начала резервирования всегда " +
                                               "раньше времени окончания!");
        }
    }

    public void setFinishTime(LocalTime finishTime) {
        if(this.getStartTime().isBefore(finishTime)) {
            this.finishTime = finishTime;
        } else {
            throw new ReservationUnitException("Время начала резервирования всегда " +
                                               "раньше времени окончания!");
        }
    }
}
