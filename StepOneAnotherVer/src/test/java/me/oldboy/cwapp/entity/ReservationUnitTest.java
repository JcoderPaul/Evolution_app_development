package me.oldboy.cwapp.entity;

import me.oldboy.cwapp.exception.ReservationUnitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationUnitTest {

    private LocalDate testData;
    private Long placeId;
    private Long userId;
    private LocalTime startTime;
    private LocalTime finishTime;

    @BeforeEach
    public void setUp(){
        testData = LocalDate.of(1999, 3,13);
        placeId = 1L;
        userId = 3L;
        startTime = LocalTime.of(12,00,00);
        finishTime = LocalTime.of(14,00,00);
    }

    @Test
    void constructorReservationGoodTest() {
        Reservation createReservation = new Reservation(testData, placeId, userId, startTime, finishTime);

        assertThat(createReservation.getReservationPlaceId().equals(placeId)).isTrue();
        assertThat(createReservation.getReservationUserId().equals(userId)).isTrue();
        assertThat(createReservation.getStartTime().equals(startTime)).isTrue();
        assertThat(createReservation.getFinishTime().equals(finishTime)).isTrue();
        assertThat(createReservation.getReservationDate().equals(testData)).isTrue();
    }

    @Test
    void constructorReservationExceptionTest() {
        LocalTime finishTime = LocalTime.of(11,00,00);

        assertThatThrownBy(() -> new Reservation(testData, placeId, userId, startTime, finishTime))
                .isInstanceOf(ReservationUnitException.class)
                .hasMessageContaining("Время начала резервирования всегда раньше времени окончания!");

    }

    @Test
    void setStartTimeGoodTest() {
        LocalTime updateStartTime = LocalTime.of(10,00,00);
        Reservation createReservation = new Reservation(testData, placeId, userId, startTime, finishTime);
        createReservation.setStartTime(updateStartTime);

        assertThat(createReservation.getStartTime()).isEqualTo(updateStartTime);
    }

    @Test
    void setStartTimeExceptionTest() {
        LocalTime updateStartTime = LocalTime.of(18,00,00);
        Reservation createReservation = new Reservation(testData, placeId, userId, startTime, finishTime);

        assertThatThrownBy(()->createReservation.setStartTime(updateStartTime))
                .isInstanceOf(ReservationUnitException.class)
                .hasMessageContaining("Время начала резервирования всегда " +
                                                "раньше времени окончания!");
    }

    @Test
    void setFinishTimeGoodTest() {
        LocalTime updateFinishTime = LocalTime.of(18,00,00);
        Reservation createReservation = new Reservation(testData, placeId, userId, startTime, finishTime);
        createReservation.setFinishTime(updateFinishTime);

        assertThat(createReservation.getFinishTime()).isEqualTo(updateFinishTime);
    }

    @Test
    void setFinishTimeExceptionTest() {
        LocalTime updateFinishTime = LocalTime.of(10,00,00);
        Reservation createReservation = new Reservation(testData, placeId, userId, startTime, finishTime);

        assertThatThrownBy(()->createReservation.setFinishTime(updateFinishTime))
                .isInstanceOf(ReservationUnitException.class)
                .hasMessageContaining("Время начала резервирования всегда " +
                                                "раньше времени окончания!");
    }
}