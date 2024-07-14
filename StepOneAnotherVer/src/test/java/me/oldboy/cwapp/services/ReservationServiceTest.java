package me.oldboy.cwapp.services;

import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.exception.service_exception.ReservationServiceException;
import me.oldboy.cwapp.store.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationService reservationService;
    private Long testReservationId;
    private LocalDate testReservationDate;
    private Long testReservationPlaceId;
    private Long testReservationUserId;
    private LocalTime testStartTime;
    private LocalTime testFinishTime;
    private Reservation testReservation;
    private List<Reservation> testReservationList;

    @BeforeEach
    public void setUp(){
        testReservationId = 1L;
        testReservationDate = LocalDate.of(2028,5,16);
        testReservationPlaceId = 1L;
        testReservationUserId = 5L;
        testStartTime = LocalTime.of(14,00,00);
        testFinishTime = LocalTime.of(16,00,00);
        testReservation = new Reservation(testReservationDate,
                                          testReservationPlaceId,
                                          testReservationUserId,
                                          testStartTime,
                                          testFinishTime);
        testReservationList = List.of(new Reservation(), new Reservation(), new Reservation());

        MockitoAnnotations.openMocks(this);
    }

    /* C - Метод createReservation - тестируем создание нового резервирования */

    @Test
    void shouldReturnReservationId_createReservationGoodTest() {
        when(reservationRepository.findByDateAndPlace(testReservation.getReservationDate(),
                                                      testReservation.getReservationPlaceId()))
                .thenReturn(Optional.of(testReservationList));
        when(reservationRepository.isReservationConflict(testReservation)).thenReturn(false);
        when(reservationRepository.create(testReservation)).thenReturn(testReservationId);
        assertThat(reservationService.createReservation(testReservation)).isEqualTo(testReservationId);
    }

    @Test
    void shouldReturnException_createReservationConflictTest() {
        when(reservationRepository.findByDateAndPlace(testReservation.getReservationDate(),
                                                      testReservation.getReservationPlaceId()))
                .thenReturn(Optional.of(testReservationList));
        when(reservationRepository.isReservationConflict(testReservation)).thenReturn(true);
        assertThatThrownBy(()->reservationService.createReservation(testReservation))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Выбранный временной отрезок занят! Конфликт резервирования!");
    }

    /* R - Метод findReservationById - тестируем поиск брони по её ID */

    @Test
    void shouldReturnFindReservation_findReservationByIdGoodTest() {
        when(reservationRepository.findById(testReservationId))
                .thenReturn(Optional.of(testReservation));
        assertThat(reservationService.findReservationById(testReservationId))
                .isEqualTo(testReservation);
    }

    @Test
    void shouldReturnException_findNonExistentReservationByIdTest() {
        when(reservationRepository.findById(testReservationId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(()->reservationService.findReservationById(testReservationId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Резервирование с Id - " + testReservationId + " не найдено!");
    }

    /* R - Метод findReservationByDate - тестируем поиск брони по её дате */

    @Test
    void shouldReturnFindReservation_findReservationByDateGoodTest() {
        when(reservationRepository.findByDate(testReservationDate))
                .thenReturn(Optional.of(testReservationList));
        assertThat(reservationService.findReservationByDate(testReservationDate))
                .isEqualTo(testReservationList);
    }

    @Test
    void shouldReturnException_findNonExistentReservationByDateTest() {
        when(reservationRepository.findByDate(testReservationDate))
                .thenReturn(Optional.empty());
        assertThatThrownBy(()->reservationService.findReservationByDate(testReservationDate))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("На " + testReservationDate + " бронирования отсутствуют!");
    }

    /* R - Метод findReservationByUserId - тестируем поиск брони по ID пользователя */

    @Test
    void shouldReturnFindReservation_findReservationByUserIdGoodTest() {
        when(reservationRepository.findByUserId(testReservationUserId))
                .thenReturn(Optional.of(testReservationList));
        assertThat(reservationService.findReservationByUserId(testReservationUserId))
                .isEqualTo(testReservationList);
    }

    @Test
    void shouldReturnException_findNonExistentReservationByUserIdTest() {
        when(reservationRepository.findByUserId(testReservationUserId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(()->reservationService.findReservationByUserId(testReservationUserId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Пользователь с ID - " +
                                                testReservationUserId +
                                                " бронирований не имеет!");
    }

    /* R - Метод findReservationByPlaceId - тестируем поиск брони по ID рабочего места/зала */

    @Test
    void shouldReturnFindReservation_findReservationByPlaceIdGoodTest() {
        when(reservationRepository.findByPlaceId(testReservationPlaceId))
                .thenReturn(Optional.of(testReservationList));
        assertThat(reservationService.findReservationByPlaceId(testReservationPlaceId))
                .isEqualTo(testReservationList);
    }

    @Test
    void shouldReturnException_findNonExistentReservationByPlaceIdTest() {
        when(reservationRepository.findByPlaceId(testReservationPlaceId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(()->reservationService.findReservationByPlaceId(testReservationPlaceId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Рабочее место/зал с ID - " +
                                                testReservationPlaceId +
                                                " не забронированы!");
    }

    /* R - Метод findReservationByDateAndPlace - тестируем поиск брони по дате и ID рабочего места/зала */

    @Test
    void shouldReturnFindReservation_findReservationByDateAndPlaceIdGoodTest() {
        when(reservationRepository.findByDateAndPlace(testReservationDate, testReservationPlaceId))
                .thenReturn(Optional.of(testReservationList));
        assertThat(reservationService.findReservationByDateAndPlace(testReservationDate, testReservationPlaceId))
                .isEqualTo(testReservationList);
    }

    @Test
    void shouldReturnException_findNonExistentReservationByDateAndPlaceIdTest() {
        when(reservationRepository.findByDateAndPlace(testReservationDate, testReservationPlaceId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(()->reservationService.findReservationByDateAndPlace(testReservationDate,
                                                                                testReservationPlaceId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Рабочее место/зал с ID - " + testReservationPlaceId +
                                                " не имеет броней на: " + testReservationDate);
    }

    /* R - Метод findAllReservation - тестируем поиск всех броней */

    @Test
    void shouldReturnAllReservation_findAllReservationGoodTest() {
        when(reservationRepository.findAll()).thenReturn(testReservationList);
        assertThat(reservationService.findAllReservation())
                .isEqualTo(testReservationList);
    }

    @Test
    void shouldReturnException_findNonExistentReservationBaseIsClearOrNonConnectionTest() {
        when(reservationRepository.findAll())
                .thenReturn(new ArrayList<>());
        assertThatThrownBy(()->reservationService.findAllReservation())
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("База броней пуста / ошибка связи с БД!");
    }

    /* U - Метод updateReservation - тестируем обновление существующего резервирования */

    @Test
    void shouldReturnUpdatedReservation_updateReservationGoodTest() {
        Reservation reservationForUpdate = testReservation;
        testReservation.setReservationId(testReservationId);
        testReservation.setReservationDate(LocalDate.of(2120, 5,1));
        testReservation.setReservationPlaceId(7L);
        when(reservationRepository.findById(reservationForUpdate.getReservationId()))
                .thenReturn(Optional.of(testReservation));
        when(reservationRepository.isReservationConflict(testReservation)).thenReturn(false);
        when(reservationRepository.update(reservationForUpdate)).thenReturn(testReservation);
        assertThat(reservationService.updateReservation(reservationForUpdate)).isEqualTo(testReservation);
    }

    @Test
    void shouldReturnException_updateReservationConflictTest() {
        Reservation reservationForUpdate = testReservation;
        testReservation.setReservationId(testReservationId);
        testReservation.setReservationDate(LocalDate.of(2120, 5,1));
        testReservation.setReservationPlaceId(7L);
        when(reservationRepository.findById(reservationForUpdate.getReservationId()))
                .thenReturn(Optional.of(testReservation));
        when(reservationRepository.isReservationConflict(testReservation)).thenReturn(true);
        assertThatThrownBy(()->reservationService.updateReservation(reservationForUpdate))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Обновление резервирования невозможно! Конфликт диапазонов!");
    }

    /* D - Метод deleteReservation - тестируем удаление существующей брони */

    @Test
    void shouldReturnTrueAfterDeleteReservation_deleteReservationGoodTest() {
        when(reservationRepository.findById(testReservationId))
                .thenReturn(Optional.of(testReservation));
        when(reservationRepository.delete(testReservationId)).thenReturn(true);
        assertThat(reservationService.deleteReservation(testReservationId)).isTrue();
    }

    @Test
    void shouldReturnException_deleteNonExistentReservationTest() {
        when(reservationRepository.findById(testReservationId))
                .thenReturn(Optional.empty());
        when(reservationRepository.isReservationConflict(testReservation)).thenReturn(true);
        assertThatThrownBy(()->reservationService.deleteReservation(testReservationId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Брони с ID - " + testReservationId + " не существует!");
    }
}