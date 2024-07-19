package me.oldboy.cwapp.handlers;

import me.oldboy.cwapp.entity.*;
import me.oldboy.cwapp.exception.handlers_exception.ReservationHandlerException;
import me.oldboy.cwapp.handlers.ReservationViewHandler;
import me.oldboy.cwapp.services.PlaceService;
import me.oldboy.cwapp.services.ReservationService;
import me.oldboy.cwapp.services.UserService;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ReservationHandlerTest {

    @Mock
    private ReservationService reservationService;
    @Mock
    private PlaceService placeService;
    @Mock
    private UserService userService;
    @InjectMocks
    private ReservationViewHandler reservationHandler;

    private Scanner scanner;
    private Long testUserIdForReservation;
    private Long testPlaceIdForReservation;
    private Long testReservationId;
    private ByteArrayInputStream inScanner;
    private ByteArrayOutputStream outScreen;
    private List<Reservation> testReservationList;
    private LocalDate testDate;
    private LocalTime testTimeStart;
    private LocalTime testTimeFinish;
    private User testUser;
    private Place testPlace;

    private String testOutString =
            "Бронь с ID - 1 на 2025-09-19 сделана на: Place [placeId: 5, species: HALL, placeNumber: 3] " +
            "на время 10:00 - 12:00 " +
            "принадлежит: User [userId: 3, userLogin: 'User', passWord: 'user', role: USER]\r\n" +
            "Бронь с ID - 2 на 2025-09-19 сделана на: Place [placeId: 5, species: HALL, placeNumber: 3] " +
            "на время 13:00 - 14:00 " +
            "принадлежит: User [userId: 3, userLogin: 'User', passWord: 'user', role: USER]\r\n" +
            "Бронь с ID - 3 на 2025-09-19 сделана на: Place [placeId: 5, species: HALL, placeNumber: 3] " +
            "на время 15:00 - 16:00 " +
            "принадлежит: User [userId: 3, userLogin: 'User', passWord: 'user', role: USER]";

    @BeforeEach
    public void setUp(){
        testUserIdForReservation = 3L;
        testPlaceIdForReservation = 5L;
        testReservationId = 2L;
        testUser = new User(testUserIdForReservation, "User","user", Role.USER);
        testPlace = new Place(testPlaceIdForReservation, Species.HALL, 3);
        testDate = LocalDate.of(2025,9,19);
        testTimeStart = LocalTime.of(10,00);
        testTimeFinish = LocalTime.of(12,00);

        testReservationList = new ArrayList<>();

        testReservationList.add(new Reservation(1L,
                testDate,
                testPlaceIdForReservation,
                testUserIdForReservation,
                testTimeStart,
                testTimeFinish));
        testReservationList.add(new Reservation(2L,
                testDate,
                testPlaceIdForReservation,
                testUserIdForReservation,
                testTimeStart.plusHours(3),
                testTimeFinish.plusHours(2)));
        testReservationList.add(new Reservation(3L,
                testDate,
                testPlaceIdForReservation,
                testUserIdForReservation,
                testTimeStart.plusHours(5),
                testTimeFinish.plusHours(4)));

        MockitoAnnotations.openMocks(this);
    }

    /* Тестируем методы создания брони */

    @Test
    void shouldReturnNewReservationHallId_reservationHallGoodTest() {
        String inMenuData = "2028-04-17\n2\n14:30\n16:30";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testUserIdForReservation))
                .thenReturn(testUser);
        when(placeService.getPlaceBySpeciesAndPlaceNumber(eq(Species.HALL), anyInt()))
                .thenReturn(new Place());
        when(reservationService.isReservationConflict(any(Reservation.class)))
                .thenReturn(false);
        when(reservationService.createReservation(any(Reservation.class)))
                .thenReturn(testReservationId);
        assertThat(reservationHandler.reservationHall(scanner,testUserIdForReservation))
                .isEqualTo(testReservationId);
    }

    @Test
    void shouldReturnNewReservationWorkplaceId_reservationWorkplaceGoodTest() {
        String inMenuData = "2028-04-17\n2\n14:30\n16:30";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testUserIdForReservation))
                .thenReturn(testUser);
        when(placeService.getPlaceBySpeciesAndPlaceNumber(eq(Species.WORKPLACE), anyInt()))
                .thenReturn(new Place());
        when(reservationService.isReservationConflict(any(Reservation.class)))
                .thenReturn(false);
        when(reservationService.createReservation(any(Reservation.class)))
                .thenReturn(testReservationId);
        assertThat(reservationHandler.reservationWorkplace(scanner,testUserIdForReservation))
                .isEqualTo(testReservationId);
    }

    /* Тестируем методы удаления брони */

    @Test
    void shouldReturnTrueIfDeletedWasSuccess_deleteHallReservationGoodTest() {
        String inMenuData = "2028-04-17\n2\n1";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(placeService.getPlaceBySpeciesAndPlaceNumber(eq(Species.HALL), anyInt()))
                .thenReturn(new Place(testPlaceIdForReservation, Species.HALL, 5));
        when(userService.getExistUserById(testUserIdForReservation))
                .thenReturn(testUser);
        when(reservationService.findReservationByDateAndPlace(any(LocalDate.class), anyLong()))
                .thenReturn(testReservationList);
        when(reservationService.deleteReservation(anyLong()))
                .thenReturn(true);
        assertThat(reservationHandler.deleteHallReservation(scanner,testUserIdForReservation))
                .isTrue();
    }

    @Test
    void shouldReturnTrueIfDeletedWasSuccess_deleteWorkplaceReservationGoodTest() {
        String inMenuData = "2028-04-17\n2\n1";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(placeService.getPlaceBySpeciesAndPlaceNumber(eq(Species.WORKPLACE), anyInt()))
                .thenReturn(new Place(testPlaceIdForReservation, Species.WORKPLACE, 5));
        when(userService.getExistUserById(testUserIdForReservation))
                .thenReturn(testUser);
        when(reservationService.findReservationByDateAndPlace(any(LocalDate.class), anyLong()))
                .thenReturn(testReservationList);
        when(reservationService.deleteReservation(anyLong()))
                .thenReturn(true);
        assertThat(reservationHandler.deleteWorkplaceReservation(scanner,testUserIdForReservation))
                .isTrue();
    }

    /* Тестируем методы отображения информации */

    @Test
    void shouldReturnViewOfAllReservation_showAllReservationTest() throws IOException {
        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(reservationService.findAllReservation()).thenReturn(testReservationList);
        when(placeService.getPlaceById(anyLong())).thenReturn(testPlace);
        when(userService.getExistUserById(testUserIdForReservation)).thenReturn(testUser);

        reservationHandler.showAllReservation();

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(testOutString))
                .isTrue();
    }

    @Test
    void shouldReturnViewOfAllReservationOfConcreteDate_showAllReservationByDateTest() throws IOException {
        String inMenuData = "2025-09-19";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(reservationService.findReservationByDate(any(LocalDate.class)))
                .thenReturn(testReservationList);
        when(placeService.getPlaceById(anyLong())).thenReturn(testPlace);
        when(userService.getExistUserById(testUserIdForReservation)).thenReturn(testUser);

        reservationHandler.showAllReservationByDate(scanner);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(testOutString)).isTrue();
    }

    @Test
    void shouldReturnViewOfAllReservationByUserId_showAllReservationByUserIdTest() throws IOException {
        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(reservationService.findReservationByUserId(anyLong()))
                .thenReturn(testReservationList);
        when(placeService.getPlaceById(anyLong())).thenReturn(testPlace);
        when(userService.getExistUserById(testUserIdForReservation)).thenReturn(testUser);

        reservationHandler.showAllReservationByUserId(testUserIdForReservation);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(testOutString)).isTrue();
    }

    @Test
    void shouldReturnViewOfAllReservationByPlaceId_showAllReservationByPlaceIdTest() throws IOException {
        String inMenuData = String.valueOf(testPlaceIdForReservation);
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(reservationService.findReservationByPlaceId(anyLong()))
                .thenReturn(testReservationList);
        when(placeService.getPlaceById(anyLong())).thenReturn(testPlace);
        when(userService.getExistUserById(testUserIdForReservation)).thenReturn(testUser);

        reservationHandler.showAllReservationByPlaceId(scanner);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(testOutString)).isTrue();
    }

    /* Тестируем броски исключений */

    @Test
    void shouldReturnException_reservationHallTest() {
        String inMenuData = "2028-04-17\n2\n14:30\n16:30";
        inScanner = new ByteArrayInputStream(inMenuData.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testUserIdForReservation))
                .thenReturn(testUser);
        when(placeService.getPlaceBySpeciesAndPlaceNumber(eq(Species.HALL), anyInt()))
                .thenReturn(new Place());
        when(reservationService.isReservationConflict(any(Reservation.class)))
                .thenReturn(true);
        assertThatThrownBy(() -> reservationHandler.reservationHall(scanner,testUserIdForReservation))
                .isInstanceOf(ReservationHandlerException.class)
                .hasMessageContaining("Конфликт времени резервирования!");
    }
}