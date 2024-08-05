package me.oldboy.cwapp.core.controllers;

import me.oldboy.cwapp.exceptions.controllers.ReserveControllerException;
import me.oldboy.cwapp.core.entity.*;
import me.oldboy.cwapp.core.service.PlaceService;
import me.oldboy.cwapp.core.service.ReserveService;
import me.oldboy.cwapp.core.service.SlotService;
import me.oldboy.cwapp.core.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReserveControllerTest {

    @Mock
    private ReserveService reservationService;
    @Mock
    private UserService userService;
    @Mock
    private PlaceService placeService;
    @Mock
    private SlotService slotService;
    @InjectMocks
    private ReserveController reserveController;

    private Scanner scanner;
    private Long testUserId;
    private Long testPlaceId;
    private Long testSlotId;
    private Long testReservationId;
    private ByteArrayInputStream inScanner;
    private ByteArrayOutputStream outScreen;
    private List<Reservation> testReservationList;
    private LocalDate testDate;
    private User testUser;
    private Place testPlace;
    private Slot testSlot;
    private Reservation testReservation;
    private String testOutString =
            "Бронь ID - 12 на 2028-04-17 зарезервирован(о): Place [placeId: 2, species: HALL, placeNumber - 2] " +
                    "слот:  Slot [ slotId: 1, slotNumber: 10, time range: 10:00 - 11:00 ] " +
                    "принадлежит: User [userId - 2, login: 'User', password: 'user', role: USER]\r\n" +
            "Бронь ID - 13 на 2028-04-17 зарезервирован(о): Place [placeId: 13, species: WORKPLACE, placeNumber - 13] " +
                    "слот:  Slot [ slotId: 10, slotNumber: 19, time range: 19:00 - 20:00 ] " +
                    "принадлежит: User [userId - 2, login: 'User', password: 'user', role: USER]\r\n" +
            "Бронь ID - 14 на 2028-04-17 зарезервирован(о): Place [placeId: 14, species: WORKPLACE, placeNumber - 14] " +
                    "слот:  Slot [ slotId: 11, slotNumber: 20, time range: 20:00 - 21:00 ] " +
                    "принадлежит: User [userId - 2, login: 'User', password: 'user', role: USER]";

    @BeforeEach
    public void setUp(){
        testUserId = 2L;
        testPlaceId = 2L;
        testSlotId = 1L;
        testReservationId = 12L;

        testUser = new User(testUserId, "User","user", Role.USER);
        testPlace = new Place(testPlaceId, Species.HALL, 2);
        testSlot = new Slot(testSlotId, 10, LocalTime.parse("10:00"), LocalTime.parse("11:00"));
        testDate = LocalDate.parse("2028-04-17");
        testReservation = new Reservation(testReservationId, testDate, testUser, testPlace, testSlot);

        testReservationList = new ArrayList<>();

        testReservationList.add(testReservation);
        testReservationList.add(new Reservation(13L,
                testDate,
                testUser,
                new Place(13L, Species.WORKPLACE, 13),
                new Slot(10L, 19, LocalTime.parse("19:00"), LocalTime.parse("20:00"))));
        testReservationList.add(new Reservation(14L,
                testDate,
                testUser,
                new Place(14L, Species.WORKPLACE, 14),
                new Slot(11L, 20, LocalTime.parse("20:00"), LocalTime.parse("21:00"))));

        MockitoAnnotations.openMocks(this);
    }

    /* Тестируем метод *.reservationPlace() создания брони */

    @Test
    void shouldReturnNewReservationHallId_reservationPlaceTest() {
        String inMenuData = "зал\n2028-04-17\n2\n10";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testUserId)).thenReturn(testUser);
        when(placeService.findPlaceBySpeciesAndNumber(any(Species.class), anyInt())).thenReturn(testPlace);
        when(slotService.findSlotByNumber(anyInt())).thenReturn(testSlot);
        when(reservationService.findReservationsByDatePlaceAndSlotId(testDate,
                                                                     testPlaceId,
                                                                     testSlotId)).thenReturn(null);
        when(reservationService.createReservation(any(Reservation.class))).thenReturn(testReservationId);

        assertThat(reserveController.reservationPlace(scanner, testUserId)).isEqualTo(testReservationId);
    }

    @Test
    void shouldReturnNewReservationWorkplaceId_reservationPlaceTest() {
        String inMenuData = "место\n2028-04-17\n2\n10";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testUserId)).thenReturn(testUser);
        when(placeService.findPlaceBySpeciesAndNumber(any(Species.class), anyInt())).thenReturn(testPlace);
        when(slotService.findSlotByNumber(anyInt())).thenReturn(testSlot);
        when(reservationService.findReservationsByDatePlaceAndSlotId(testDate,
                                                                     testPlaceId,
                                                                     testSlotId)).thenReturn(null);
        when(reservationService.createReservation(any(Reservation.class))).thenReturn(testReservationId);

        assertThat(reserveController.reservationPlace(scanner, testUserId)).isEqualTo(testReservationId);
    }

    @Test
    void shouldReturnExceptionReservationConflict_reservationPlaceTest() {
        String inMenuData = "зал\n2028-04-17\n2\n10";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testUserId)).thenReturn(testUser);
        when(placeService.findPlaceBySpeciesAndNumber(eq(Species.HALL), anyInt())).thenReturn(testPlace);
        when(slotService.findSlotByNumber(anyInt())).thenReturn(testSlot);
        when(reservationService.findReservationsByDatePlaceAndSlotId(testDate,
                                                                     testPlaceId,
                                                                     testSlotId)).thenReturn(testReservation);

        assertThatThrownBy(() -> reserveController.reservationPlace(scanner, testUserId))
                .isInstanceOf(ReserveControllerException.class)
                .hasMessageContaining("Повторное резервирование места и слота на ту же дату недопустимо!");
    }

    @Test
    void shouldReturnExceptionWrongPlaceEnter_reservationPlaceTest() {
        String inMenuData = "стул\n2028-04-17\n2\n10";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        assertThatThrownBy(() -> reserveController.reservationPlace(scanner, testUserId))
                .isInstanceOf(ReserveControllerException.class)
                .hasMessageContaining("Вы ввели ресурс не верно (повторите: 'зал' или 'место')!");
    }

    /* Тестируем метод *.deletePlaceReservation() удаления брони */

    @Test
    void shouldReturnTrue_deletePlaceReservationTest() {
        String inMenuData = "зал\n2028-04-17\n2\n10";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testUserId)).thenReturn(testUser);
        when(placeService.findPlaceBySpeciesAndNumber(any(Species.class), anyInt())).thenReturn(testPlace);
        when(slotService.findSlotByNumber(anyInt())).thenReturn(testSlot);
        when(reservationService.findReservationsByDatePlaceAndSlotId(testDate,
                                                                     testPlaceId,
                                                                     testSlotId)).thenReturn(testReservation);
        when(reservationService.deleteReservation(testReservationId)).thenReturn(true);

        assertThat(reserveController.deletePlaceReservation(scanner, testUserId)).isEqualTo(true);
    }

    @Test
    void shouldReturnExceptionHaveNoReservation_deletePlaceReservationTest() {
        String inMenuData = "зал\n2028-04-17\n2\n10";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testUserId)).thenReturn(testUser);
        when(placeService.findPlaceBySpeciesAndNumber(any(Species.class), anyInt())).thenReturn(testPlace);
        when(slotService.findSlotByNumber(anyInt())).thenReturn(testSlot);
        when(reservationService.findReservationsByDatePlaceAndSlotId(testDate,
                testPlaceId,
                testSlotId)).thenReturn(null);

        assertThatThrownBy(() -> reserveController.deletePlaceReservation(scanner, testUserId))
                .isInstanceOf(ReserveControllerException.class)
                .hasMessageContaining("Брони с указанными параметрами не найдено!");
    }

    /* Тестируем метод *.showAllReservation() отображение всех броней */

    @Test
    void showAllReservationTest() throws IOException {
        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(reservationService.findAllReservation()).thenReturn(testReservationList);

        reserveController.showAllReservation();

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(testOutString)).isTrue();
    }

    /* Тестируем метод *.showAllReservationByDate() отображение всех броней по Дате */

    @Test
    void showAllReservationByDateTest() throws IOException {
        String inMenuData = "2028-04-17";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(reservationService.findReservationByDate(testDate))
                .thenReturn(testReservationList);

        reserveController.showAllReservationByDate(scanner);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(testOutString)).isTrue();
    }

    /* Тестируем метод *.showAllReservationByDate() отображение всех броней по ID пользователя */

    @Test
    void showAllReservationByUserIdTest() throws IOException {
        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(reservationService.findReservationsByUserId(anyLong()))
                .thenReturn(testReservationList);

        reserveController.showAllReservationByUserId(testUserId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(testOutString)).isTrue();
    }

    /* Тестируем метод *.showAllReservationByDate() отображение всех броней по ID места (HALL / WORKPLACE) */

    @Test
    void showAllReservationByPlaceIdTest() throws IOException {
        String inMenuData = String.valueOf(testPlaceId);
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(reservationService.findReservationByPlaceId(anyLong()))
                .thenReturn(testReservationList);

        reserveController.showAllReservationByPlaceId(scanner);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(testOutString)).isTrue();
    }
}