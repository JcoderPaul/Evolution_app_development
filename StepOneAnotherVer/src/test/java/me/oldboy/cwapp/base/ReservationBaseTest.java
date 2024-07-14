package me.oldboy.cwapp.base;

import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.exception.base_exception.ReservationBaseException;
import me.oldboy.cwapp.store.base.ReservationBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationBaseTest {

    private ReservationBase reservationBase;

    private Reservation testReservation;

    /* Проверяем нормальную работу методов */

    @BeforeEach
    public void initBase(){
        testReservation = new Reservation(LocalDate.of(2034,9,19),
                1L,
                2L,
                LocalTime.of(12,00,00),
                LocalTime.of(14,00,00));
        reservationBase = new ReservationBase();
    }

    @AfterEach
    public void killBase(){
        reservationBase.getReservationBase().clear();
    }

    @Test
    void createReservationGoodTest() {
        Reservation testCreate_1 = testReservation;
        Long createReservation = reservationBase.create(testCreate_1);
        assertThat(createReservation).isEqualTo(1L);

        Reservation testCreate_2 = testReservation;
        testReservation.setReservationId(null);
        testReservation.setReservationDate(LocalDate.of(2056,2,15));
        Long createReservation_2 = reservationBase.create(testCreate_2);
        assertThat(createReservation_2).isEqualTo(2L);

        Reservation testCreate_3 = testReservation;
        testReservation.setStartTime(LocalTime.of(10,00,00));
        testReservation.setReservationId(null);
        Long createReservation_3 = reservationBase.create(testCreate_3);
        assertThat(createReservation_3).isEqualTo(3L);
    }

    @Test
    void findReservationByIdGoodTest() {
        Reservation createReservation = testReservation;
        Long justNowCreateReservationId = reservationBase.create(createReservation);

        Boolean mayBeReservation = reservationBase.findById(justNowCreateReservationId).isPresent();
        assertThat(mayBeReservation).isTrue();
        assertThat(reservationBase.findById(justNowCreateReservationId).get()).isEqualTo(createReservation);
    }

    @Test
    void updateReservationGoodTest() {
        Reservation reservationToUpdate = testReservation;
        Long justNowReservationId = reservationBase.create(reservationToUpdate);

        Reservation findReservationForUpdate = reservationBase.findById(justNowReservationId).get();
        findReservationForUpdate.setReservationDate(LocalDate.of(2045,6,15));
        findReservationForUpdate.setReservationPlaceId(1L);
        findReservationForUpdate.setStartTime(LocalTime.of(11,30,00));

        assertThat(reservationBase.update(findReservationForUpdate)).isEqualTo(reservationToUpdate);
    }

    @Test
    void deleteReservationGoodTest() {
        Reservation reservationForDelete = testReservation;
        Long createdReservationId = reservationBase.create(reservationForDelete);

        assertThat(reservationBase.delete(createdReservationId)).isTrue();
    }

    @Test
    void findReservesByDateGoodTest() {
        LocalDate testDate = LocalDate.of(2056,2,15);

        Reservation testCreate_1 = new Reservation();
        testCreate_1.setReservationDate(LocalDate.of(1919,4,18));
        reservationBase.create(testCreate_1);

        Reservation testCreate_2 = new Reservation();
        testCreate_2.setReservationDate(testDate);
        reservationBase.create(testCreate_2);

        Reservation testCreate_3 = new Reservation();
        testCreate_3.setReservationDate(testDate);
        reservationBase.create(testCreate_3);

        assertThat(reservationBase.findByDate(testDate).get().size()).isEqualTo(2);
    }

    @Test
    void findReservationsByUserIdGoodTest() {
        Long userTestId = 1L;

        Reservation testCreate_1 = new Reservation();
        testCreate_1.setReservationUserId(userTestId);
        reservationBase.create(testCreate_1);

        Reservation testCreate_2 = new Reservation();
        testCreate_2.setReservationUserId(userTestId);
        reservationBase.create(testCreate_2);

        Reservation testCreate_3 = new Reservation();
        testCreate_3.setReservationUserId(userTestId + 5L);
        reservationBase.create(testCreate_3);

        assertThat(reservationBase.findByUserId(userTestId).get().size()).isEqualTo(2);
    }

    @Test
    void findReservesByPlaceId() {
        Long placeTestId = 1L;

        Reservation testCreate_1 = new Reservation();
        testCreate_1.setReservationPlaceId(placeTestId);
        reservationBase.create(testCreate_1);

        Reservation testCreate_2 = new Reservation();
        testCreate_2.setReservationPlaceId(placeTestId);
        reservationBase.create(testCreate_2);

        Reservation testCreate_3 = new Reservation();
        testCreate_3.setReservationPlaceId(placeTestId + 4L);
        reservationBase.create(testCreate_3);

        assertThat(reservationBase.findByPlaceId(placeTestId).get().size()).isEqualTo(2);
    }

    @Test
    void findAllReserveGoodTest() {
        Long reserveId = reservationBase.create(new Reservation());
        Long reserveId_2 = reservationBase.create(new Reservation());
        Long reserveId_3 = reservationBase.create(new Reservation());
        Long reserveId_4 = reservationBase.create(new Reservation());
        Long reserveId_5 = reservationBase.create(new Reservation());

        assertThat(reservationBase.findAll().size()).isEqualTo(5);

        assertThat(reservationBase.findAll().contains(reservationBase.findById(reserveId).get())).isTrue();
        assertThat(reservationBase.findAll().contains(reservationBase.findById(reserveId_2).get())).isTrue();
        assertThat(reservationBase.findAll().contains(reservationBase.findById(reserveId_3).get())).isTrue();
        assertThat(reservationBase.findAll().contains(reservationBase.findById(reserveId_4).get())).isTrue();
        assertThat(reservationBase.findAll().contains(reservationBase.findById(reserveId_5).get())).isTrue();
    }

    @Test
    void findReserveByDateAndPlaceGoodTest() {
        Long placeTestId = 1L;
        LocalDate reservationTestDate = LocalDate.of(1878, 5,12);

        Reservation testCreate_1 = new Reservation();
        testCreate_1.setReservationPlaceId(placeTestId);
        testCreate_1.setReservationDate(reservationTestDate);
        reservationBase.create(testCreate_1);

        Reservation testCreate_2 = new Reservation();
        testCreate_2.setReservationPlaceId(placeTestId + 4L);
        testCreate_2.setReservationDate(reservationTestDate.plusDays(5));
        reservationBase.create(testCreate_2);

        Reservation testCreate_3 = new Reservation();
        testCreate_3.setReservationPlaceId(placeTestId);
        testCreate_3.setReservationDate(reservationTestDate);
        reservationBase.create(testCreate_3);

        assertThat(reservationBase.findByDateAndPlace(reservationTestDate, placeTestId)
                                  .get()
                                  .size())
                                  .isEqualTo(2);
    }

    @Test
    void isReservationNoConflictTest() {
        Long placeTestId = 1L;
        Long userTestId = 5L;
        LocalDate reservationTestDate = LocalDate.of(1878, 5,12);
        LocalTime testStartTime = LocalTime.of(14,00,00);
        LocalTime testFinishTime = LocalTime.of(16,00,00);

        Reservation testCreate_1 = new Reservation(reservationTestDate,
                                                   placeTestId,
                                                   userTestId,
                                                   testStartTime,
                                                   testFinishTime);
        reservationBase.create(testCreate_1);

        Reservation testCreate_2 = new Reservation(reservationTestDate,
                                                   placeTestId,
                                                   userTestId,
                                                   testStartTime.plusHours(3),
                                                   testFinishTime.plusHours(2));
        reservationBase.create(testCreate_2);

        Reservation testCreate_3 = new Reservation(reservationTestDate,
                                                   placeTestId,
                                                   userTestId,
                                                   testStartTime.minusHours(4),
                                                   testFinishTime.minusHours(4));
        reservationBase.create(testCreate_3);

        Reservation testCreate_4 = new Reservation(reservationTestDate,
                                                   placeTestId,
                                                   userTestId,
                                                   testStartTime.minusHours(6),
                                                   testFinishTime.minusHours(6));

        assertThat(reservationBase.isReservationConflict(testCreate_4)).isFalse();
    }

    @Test
    void isReservationConflictedTest() {
        Long placeTestId = 1L;
        Long userTestId = 5L;
        LocalDate reservationTestDate = LocalDate.of(1878, 5,12);
        LocalTime testStartTime = LocalTime.of(14,00,00);
        LocalTime testFinishTime = LocalTime.of(16,00,00);

        Reservation testCreate_1 = new Reservation(reservationTestDate,
                placeTestId,
                userTestId,
                testStartTime,
                testFinishTime);
        reservationBase.create(testCreate_1);

        Reservation testCreate_2 = new Reservation(reservationTestDate,
                placeTestId,
                userTestId,
                testStartTime.plusHours(1),
                testFinishTime.plusHours(2));
        assertThat(reservationBase.isReservationConflict(testCreate_2)).isTrue();

        Reservation testCreate_3 = new Reservation(reservationTestDate,
                placeTestId,
                userTestId,
                testStartTime.minusHours(1),
                testFinishTime.minusHours(1));

        assertThat(reservationBase.isReservationConflict(testCreate_3)).isTrue();

        Reservation testCreate_4 = new Reservation(reservationTestDate,
                placeTestId,
                userTestId,
                testStartTime.plusMinutes(30),
                testFinishTime.minusMinutes(10));

        assertThat(reservationBase.isReservationConflict(testCreate_4)).isTrue();
    }

    /* Проверяем броски исключений */

    @Test
    void createReservationWithIdExceptionTest() {
        testReservation.setReservationId(2L);
        assertThatThrownBy(()->reservationBase.create(testReservation))
                .isInstanceOf(ReservationBaseException.class)
                .hasMessageContaining("Ошибка резервирования!");
    }

    @Test
    void findNonExistentReservationByIdExceptionTest() {
        Long nonExistentReservationId = 2L;
        assertThat(reservationBase.findById(nonExistentReservationId))
                                  .isEqualTo(Optional.ofNullable(null));
    }

    @Test
    void updateNonExistentReservationExceptionTest() {
        Reservation updateReserve = testReservation;
        testReservation.setReservationId(4L);

        assertThatThrownBy(()->reservationBase.update(updateReserve))
                .isInstanceOf(ReservationBaseException.class)
                .hasMessageContaining("Вы пытаетесь обновить несуществующею бронь!");
    }

    @Test
    void deleteNonExistentReservationExceptionTest() {
        Reservation deleteReserveFor = testReservation;
        testReservation.setReservationId(8L);
        assertThatThrownBy(()->reservationBase.delete(deleteReserveFor.getReservationId()))
                .isInstanceOf(ReservationBaseException.class)
                .hasMessageContaining("Бронь с ID: " + deleteReserveFor.getReservationId() + " в базе не найдена!");
    }
}