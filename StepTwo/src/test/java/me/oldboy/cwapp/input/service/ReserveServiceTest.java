package me.oldboy.cwapp.input.service;

import me.oldboy.cwapp.exceptions.services.ReserveServiceException;
import me.oldboy.cwapp.input.entity.*;
import me.oldboy.cwapp.input.repository.crud.PlaceRepository;
import me.oldboy.cwapp.input.repository.crud.ReservationRepository;
import me.oldboy.cwapp.input.repository.crud.SlotRepository;
import me.oldboy.cwapp.input.repository.crud.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

class ReserveServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private PlaceRepository placeRepository;
    @InjectMocks
    private ReserveService reserveService;

    private Long testReservationId;
    private LocalDate testReservationDate;
    private User testUser;
    private Place testPlace;
    private Slot testSlot;
    private Reservation testReservationWithoutId;
    private Reservation testReservationWithId;
    private List<Reservation> testReservationBase;

    @BeforeEach
    public void setUp(){
        testReservationId = 10L;
        testReservationDate = LocalDate.parse("2025-09-09");
        testUser = new User(4L, "Good_user", "123", Role.ADMIN);
        testPlace = new Place(12L, Species.HALL, 5);
        testSlot = new Slot(19L, 19, LocalTime.parse("19:00"), LocalTime.parse("20:00"));
        testReservationWithoutId = new Reservation(testReservationDate, testUser, testPlace, testSlot);
        testReservationWithId = new Reservation(testReservationId, testReservationDate, testUser, testPlace, testSlot);

//        emptyPlaceBase = new ArrayList<>();
//        testPlaceBase = List.of(new Place(), new Place(), new Place());
//        emptyReservationBase = new ArrayList<>();
        testReservationBase = List.of(new Reservation(), new Reservation(), new Reservation());

        MockitoAnnotations.openMocks(this);
    }
    /* Тестируем метод *.createReservation() условного уровня сервисов */

    @Test
    void shouldReturnNewReservationId_createReservationTest() {
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.of(testSlot));
        when(reservationRepository.createReservation(testReservationWithoutId))
                .thenReturn(Optional.of(testReservationWithId));

        assertThat(reserveService.createReservation(testReservationWithoutId)).isEqualTo(testReservationId);
    }

    @Test
    void shouldReturnExceptionIfUserIdNotFound_createReservationTest() {
        testReservationWithoutId.setUser(new User());
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.createReservation(testReservationWithoutId))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Делающий бронь пользователь не найден в БД!");
    }

    @Test
    void shouldReturnExceptionIfPlaceIdNotFound_createReservationTest() {
        testReservationWithoutId.setPlace(new Place());
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.createReservation(testReservationWithoutId))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Попытка забронировать несуществующий зал/место!");
    }

    @Test
    void shouldReturnExceptionIfSlotIdNotFound_createReservationTest() {
        testReservationWithoutId.setSlot(new Slot());
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.createReservation(testReservationWithoutId))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Попытка забронировать несуществующий слот!");
    }

    @Test
    void shouldReturnExceptionIfDuplicateReservation_createReservationTest() {
        testReservationWithoutId.setSlot(new Slot());
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.of(testSlot));
        when(reservationRepository.findReservationByDatePlaceAndSlot(testReservationDate,
                                                                     testPlace.getPlaceId(),
                                                                     testSlot.getSlotId()))
                .thenReturn(Optional.of(testReservationWithId));

        assertThatThrownBy(() -> reserveService.createReservation(testReservationWithId))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("'" + testReservationWithId.getPlace().getSpecies().getStrName() +
                                                "' - " + testReservationWithId.getPlace().getPlaceNumber() +
                                                " уже зарезервирован(о) на '" + testReservationWithId.getReserveDate() +
                                                " c " + testReservationWithId.getSlot().getTimeStart() +
                                                " до " + testReservationWithId.getSlot().getTimeFinish());
    }

    /* Тестируем метод *.findReservationById() условного уровня сервисов */

    @Test
    void shouldReturnReservation_findReservationByIdTest() {
        when(reservationRepository.findReservationById(testReservationId))
                .thenReturn(Optional.of(testReservationWithId));
        assertThat(reserveService.findReservationById(testReservationId))
                .isEqualTo(testReservationWithId);
    }

    @Test
    void shouldReturnExceptionIfNotFoundReservation_findReservationByIdTest() {
        when(reservationRepository.findReservationById(testReservationId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.findReservationById(testReservationId))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Бронь с ID - " + testReservationId + " не найдена!");
    }

    /* Тестируем метод *.findAllReservation() условного уровня сервисов */

    @Test
    void shouldReturnListOfReservation_findAllReservationTest() {
        when(reservationRepository.findAllReservation()).thenReturn(Optional.of(testReservationBase));
        assertThat(reserveService.findAllReservation().size()).isEqualTo(3);
    }

    @Test
    void shouldReturnExceptionHaveNoReservation_findAllReservationTest() {
        when(reservationRepository.findAllReservation()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.findAllReservation())
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("База броней пуста!");
    }

    @Test
    void shouldReturnExceptionEmptyReservationList_findAllReservationTest() {
        when(reservationRepository.findAllReservation()).thenReturn(Optional.of(List.of()));
        assertThatThrownBy(() -> reserveService.findAllReservation())
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("База броней пуста!");
    }

    /* Тестируем метод *.findReservationByDate() условного уровня сервисов */

    @Test
    void shouldReturnReservationList_findReservationByDateTest() {
        when(reservationRepository.findReservationByDate(testReservationDate))
                .thenReturn(Optional.of(testReservationBase));
        assertThat(reserveService.findReservationByDate(testReservationDate).size())
                .isEqualTo(3);
    }

    @Test
    void shouldReturnExceptionReservationBaseEmpty_findReservationByDateTest() {
        when(reservationRepository.findReservationByDate(testReservationDate))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.findReservationByDate(testReservationDate).size())
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Бронирований на " + testReservationDate + " не найдено!");
    }

    @Test
    void shouldReturnExceptionReservationListEmpty_findReservationByDateTest() {
        when(reservationRepository.findReservationByDate(testReservationDate))
                .thenReturn(Optional.of(List.of()));
        assertThatThrownBy(() -> reserveService.findReservationByDate(testReservationDate).size())
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Бронирований на " + testReservationDate + " не найдено!");
    }

    /* Тестируем метод *.findReservationByPlaceId() условного уровня сервисов */

    @Test
    void shouldReturnListReservation_findReservationByPlaceIdTest() {
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(reservationRepository.findReservationByPlaceId(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testReservationBase));
        assertThat(reserveService.findReservationByPlaceId(testPlace.getPlaceId()).size())
                .isEqualTo(3);
    }

    @Test
    void shouldReturnExceptionHaveNoPlace_findReservationByPlaceIdTest() {
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.findReservationByPlaceId(testPlace.getPlaceId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Место / зал с ID: " + testPlace.getPlaceId() + " не найден(о)!");
    }

    @Test
    void shouldReturnExceptionEmptyReservationBase_findReservationByPlaceIdTest() {
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(reservationRepository.findReservationByPlaceId(testPlace.getPlaceId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.findReservationByPlaceId(testPlace.getPlaceId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("У места / зала с ID - " +
                                                testPlace.getPlaceId() +
                                                " броней не найдено!");
    }

    @Test
    void shouldReturnExceptionEmptyReservationList_findReservationByPlaceIdTest() {
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(reservationRepository.findReservationByPlaceId(testPlace.getPlaceId()))
                .thenReturn(Optional.of(List.of()));
        assertThatThrownBy(() -> reserveService.findReservationByPlaceId(testPlace.getPlaceId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("У места / зала с ID - " +
                                                testPlace.getPlaceId() +
                                                " броней не найдено!");
    }

    /* Тестируем метод *.findReservationsBySlotId() условного уровня сервисов */

    @Test
    void shouldReturnReservationList_findReservationsBySlotIdTest() {
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.of(testSlot));
        when(reservationRepository.findReservationBySlotId(testSlot.getSlotId()))
                .thenReturn(Optional.of(testReservationBase));
        assertThat(reserveService.findReservationsBySlotId(testSlot.getSlotId()).size())
                .isEqualTo(3);
    }

    @Test
    void shouldReturnExceptionHaveNoSlotId_findReservationsBySlotIdTest() {
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.findReservationsBySlotId(testSlot.getSlotId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Слот с ID - " + testSlot.getSlotId() + " не найден!");
    }

    @Test
    void shouldReturnExceptionEmptyReservationBase_findReservationsBySlotIdTest() {
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.of(testSlot));
        when(reservationRepository.findReservationBySlotId(testSlot.getSlotId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.findReservationsBySlotId(testSlot.getSlotId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Слот с ID - " + testSlot.getSlotId() + " не забронирован!");
    }

    @Test
    void shouldReturnExceptionEmptyReservationList_findReservationsBySlotIdTest() {
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.of(testSlot));
        when(reservationRepository.findReservationBySlotId(testSlot.getSlotId()))
                .thenReturn(Optional.of(List.of()));
        assertThatThrownBy(() -> reserveService.findReservationsBySlotId(testSlot.getSlotId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Слот с ID - " + testSlot.getSlotId() + " не забронирован!");
    }

    /* Тестируем метод *.findReservationsByUserId() условного уровня сервисов */

    @Test
    void shouldReturnListReservation_findReservationsByUserIdTest() {
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(reservationRepository.findReservationByUserId(testUser.getUserId()))
                .thenReturn(Optional.of(testReservationBase));
        assertThat(reserveService.findReservationsByUserId(testUser.getUserId()).size())
                .isEqualTo(3);
    }

    @Test
    void shouldReturnExceptionHaveNoUserId_findReservationsByUserIdTest() {
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.findReservationsByUserId(testUser.getUserId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Пользователь с ID - " + testUser.getUserId() +
                                                " не найден!");
    }

    @Test
    void shouldReturnExceptionEmptyReservationBase_findReservationsByUserIdTest() {
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(reservationRepository.findReservationByUserId(testUser.getUserId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.findReservationsByUserId(testUser.getUserId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("У пользователя с ID - " + testUser.getUserId() +
                                                " нет зарезервированных залов / рабочих мест!");
    }

    @Test
    void shouldReturnExceptionEmptyReservationList_findReservationsByUserIdTest() {
        when(userRepository.findUserById(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(reservationRepository.findReservationByUserId(testUser.getUserId()))
                .thenReturn(Optional.of(List.of()));
        assertThatThrownBy(() -> reserveService.findReservationsByUserId(testUser.getUserId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("У пользователя с ID - " + testUser.getUserId() +
                                                " нет зарезервированных залов / рабочих мест!");
    }

    /* Тестируем метод *.findReservationsByDatePlaceAndSlotId() условного уровня сервисов */

    @Test
    void shouldReturnReservation_findReservationsByDatePlaceAndSlotIdTest() {
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.of(testSlot));
        when(reservationRepository.findReservationByDatePlaceAndSlot(testReservationDate,
                                                                     testPlace.getPlaceId(),
                                                                     testSlot.getSlotId()))
                .thenReturn(Optional.of(testReservationWithId));
        assertThat(reserveService.findReservationsByDatePlaceAndSlotId(testReservationDate,
                                                                       testPlace.getPlaceId(),
                                                                       testSlot.getSlotId()))
                .isEqualTo(testReservationWithId);
    }

    @Test
    void shouldReturnExceptionHaveNoPlaceId_findReservationsByDatePlaceAndSlotIdTest() {
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.findReservationsByDatePlaceAndSlotId(testReservationDate,
                                                                                     testPlace.getPlaceId(),
                                                                                     testSlot.getSlotId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Место / зал с ID - " + testPlace.getPlaceId() + " не найден(о)!");
    }

    @Test
    void shouldReturnExceptionHaveNoReservationOnDate_findReservationsByDatePlaceAndSlotIdTest() {
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.of(testSlot));
        when(reservationRepository.findReservationByDatePlaceAndSlot(testReservationDate,
                                                                     testPlace.getPlaceId(),
                                                                     testSlot.getSlotId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.findReservationsByDatePlaceAndSlotId(testReservationDate,
                testPlace.getPlaceId(),
                testSlot.getSlotId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Бронь на: " + testReservationDate +
                                                " с такими параметрами не найдена!");
    }

    @Test
    void shouldReturnExceptionHaveNoSlotId_findReservationsByDatePlaceAndSlotIdTest() {
        when(placeRepository.findPlaceById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        when(slotRepository.findSlotById(testSlot.getSlotId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.findReservationsByDatePlaceAndSlotId(testReservationDate,
                testPlace.getPlaceId(),
                testSlot.getSlotId()))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Слот с ID - " + testSlot.getSlotId() + " не найден!");
    }

    /* Тестируем метод *.updateReservation() условного уровня сервисов */

    @Test
    void shouldReturnTrue_updateReservationTest() {
        /* Данные для обновления */
        LocalDate dateForUpdate = LocalDate.parse("2028-05-05");
        User userForUpdate = new User(6L, "Luk", "go123", Role.USER);
        Place placeForUpdate = new Place(23L, Species.WORKPLACE, 15);
        Slot slotForUpdate = new Slot(12L, 20, LocalTime.parse("20:00"), LocalTime.parse("21:00"));

        /* Вносим обновления */
        testReservationWithId.setReserveDate(dateForUpdate);
        testReservationWithId.setUser(userForUpdate);
        testReservationWithId.setPlace(placeForUpdate);
        testReservationWithId.setSlot(slotForUpdate);

        /* Проверяем на корректность переданные обновления методом *.isReservationCorrect() */
        when(userRepository.findUserById(userForUpdate.getUserId())).thenReturn(Optional.of(userForUpdate));
        when(placeRepository.findPlaceById(placeForUpdate.getPlaceId())).thenReturn(Optional.of(placeForUpdate));
        when(slotRepository.findSlotById(slotForUpdate.getSlotId())).thenReturn(Optional.of(slotForUpdate));
        when(reservationRepository.findReservationByDatePlaceAndSlot(testReservationWithId.getReserveDate(),
                testReservationWithId.getPlace().getPlaceId(),
                testReservationWithId.getSlot().getSlotId()))
                .thenReturn(Optional.empty());

        /* Подменяем методы ReservationRepository в самом *.updateReservation() */
        when(reservationRepository.findReservationById(testReservationId))
                .thenReturn(Optional.of(testReservationWithId));
        when(reservationRepository.updateReservation(testReservationWithId))
                .thenReturn(true);

        assertThat(reserveService.updateReservation(testReservationWithId)).isTrue();
    }

    @Test
    void shouldReturnExceptionNotFoundReservationId_updateReservationTest() {
        when(reservationRepository.findReservationById(testReservationId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> reserveService.updateReservation(testReservationWithId))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Брони с ID - " + testReservationId + " нет в БД!");
    }

    /* Тестируем метод *.deleteReservation() условного уровня сервисов */

    @Test
    void shouldReturnTrue_deleteReservationTest() {
        when(reservationRepository.findReservationById(testReservationId))
                .thenReturn(Optional.of(testReservationWithId));
        when(reservationRepository.deleteReservation(testReservationId))
                .thenReturn(true);
        assertThat(reserveService.deleteReservation(testReservationId)).isTrue();

    }

    @Test
    void shouldReturnExceptionNotFoundReservationId_deleteReservationTest() {
        when(reservationRepository.findReservationById(testReservationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.deleteReservation(testReservationId))
                .isInstanceOf(ReserveServiceException.class)
                .hasMessageContaining("Бронь с ID: " + testReservationId + " не найдена!");
    }
}