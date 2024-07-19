package me.oldboy.cwapp.handlers;

import me.oldboy.cwapp.entity.*;
import me.oldboy.cwapp.exception.handlers_exception.PlaceViewHandlerException;
import me.oldboy.cwapp.exception.service_exception.PlaceServiceException;
import me.oldboy.cwapp.handlers.PlaceViewHandler;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class PlaceViewHandlerTest {

    @Mock
    private PlaceService placeService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private UserService userService;
    @InjectMocks
    private PlaceViewHandler placeViewHandler;

    private List<Place> testPlaceList;
    private ByteArrayOutputStream outToScreen;
    private ByteArrayInputStream inScanner;
    private Scanner scanner;
    private User testAdminUser;
    private User simpleUser;
    private Place testPlaceFor;
    private String showAllPlaces =
            "ID - 1 вид: 'Конференц-зал' номер: 1\r\n" +
            "ID - 2 вид: 'Конференц-зал' номер: 2\r\n" +
            "ID - 3 вид: 'Конференц-зал' номер: 3\r\n" +
            "ID - 4 вид: 'Рабочее место' номер: 1\r\n" +
            "ID - 5 вид: 'Рабочее место' номер: 2\r\n" +
            "ID - 6 вид: 'Рабочее место' номер: 3";

    @BeforeEach
    public void setUp(){
        testAdminUser = new User(1L, "Admin", "1234", Role.ADMIN);
        simpleUser = new User(2L, "User", "1234", Role.USER);
        testPlaceFor = new Place(8L, Species.HALL, 6);

        testPlaceList = new ArrayList<>();

        testPlaceList.add(new Place(1L, Species.HALL, 1));
        testPlaceList.add(new Place(2L, Species.HALL, 2));
        testPlaceList.add(new Place(3L, Species.HALL, 3));
        testPlaceList.add(new Place(4L, Species.WORKPLACE, 1));
        testPlaceList.add(new Place(5L, Species.WORKPLACE, 2));
        testPlaceList.add(new Place(6L, Species.WORKPLACE, 3));

        MockitoAnnotations.openMocks(this);
    }

    /* Тестируем метод отображения всех существующих в коворкинг-центре мест и залов */

    @Test
    void showAllHallsAndWorkplacesTest() throws IOException {
        outToScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outToScreen));

        when(placeService.getAllPlaces()).thenReturn(testPlaceList);
        placeViewHandler.showAllHallsAndWorkplaces();

        outToScreen.flush();
        String allWrittenLines = new String(outToScreen.toByteArray());

        assertThat(allWrittenLines.contains(showAllPlaces)).isTrue();
    }

    /* Тестируем метод создания в коворкинг-центре места или зала */

    @Test
    void shouldReturnCreatedPlaceId_createNewPlaceByAdminUserTest() throws IOException {
        String strCommand = "зал\n6";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(placeService.isPlaceExist(any(Species.class), anyInt())).thenReturn(false);
        when(placeService.addNewPlace(any(Place.class))).thenReturn(testPlaceFor.getPlaceId());

        assertThat(placeViewHandler.createNewPlace(scanner, testAdminUser.getUserId())
                .equals(testPlaceFor.getPlaceId()));
    }

    @Test
    void shouldReturnException_createNewPlaceBySimpleUserTest() throws IOException {
        when(userService.getExistUserById(simpleUser.getUserId())).thenReturn(simpleUser);

        assertThatThrownBy(()->placeViewHandler.createNewPlace(scanner, simpleUser.getUserId()))
                .isInstanceOf(PlaceViewHandlerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }

    @Test
    void shouldReturnException_createDuplicatePlaceByAdminUserTest() throws IOException {
        String strCommand = "зал\n6";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(placeService.isPlaceExist(any(Species.class), anyInt())).thenReturn(true);

        assertThatThrownBy(()->placeViewHandler.createNewPlace(scanner, testAdminUser.getUserId()))
                .isInstanceOf(PlaceViewHandlerException.class)
                .hasMessageContaining("Невозможно создать дубликат '" +
                                                testPlaceFor.getSpecies().getStrName() +
                                                "' !");
    }

    /* Тестируем метод удаления существующего в коворкинг-центре места и зала */

    @Test
    void shouldReturnTrueIfPlaceDeleteSuccess_deletePlaceByAdminUserTest() throws IOException {
        String strCommand = "8";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(placeService.isPlaceExist(testPlaceFor.getPlaceId())).thenReturn(true);
        when(reservationService.findReservationByPlaceId(testPlaceFor.getPlaceId())).thenReturn(List.of());
        when(placeService.deletePlace(testPlaceFor.getPlaceId())).thenReturn(true);

        assertThat(placeViewHandler.deletePlace(scanner, testAdminUser.getUserId())).isTrue();
    }

    @Test
    void shouldReturnException_deleteReservationPlaceByAdminUserTest() throws IOException {
        String strCommand = "8";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(placeService.isPlaceExist(testPlaceFor.getPlaceId())).thenReturn(true);
        when(reservationService.findReservationByPlaceId(testPlaceFor.getPlaceId())).thenReturn(List.of(new Reservation()));

        assertThatThrownBy(()->placeViewHandler.deletePlace(scanner, testAdminUser.getUserId()))
                .isInstanceOf(PlaceViewHandlerException.class)
                .hasMessageContaining("Нельзя удалять зарезервированные места/залы");
    }

    @Test
    void shouldReturnException_deleteReservationPlaceBySimpleUserTest() throws IOException {
        when(userService.getExistUserById(simpleUser.getUserId())).thenReturn(simpleUser);

        assertThatThrownBy(()->placeViewHandler.deletePlace(scanner, simpleUser.getUserId()))
                .isInstanceOf(PlaceViewHandlerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }

    /* Тестируем метод обновления существующего в коворкинг-центре места и зала */

    @Test
    void shouldReturnUpdatingPlace_updateExistPlaceByAdminUserTest() throws IOException {
        String strCommand = "8\n12";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(reservationService.findReservationByPlaceId(testPlaceFor.getPlaceId())).thenReturn(List.of());
        when(placeService.getPlaceById(testPlaceFor.getPlaceId())).thenReturn(testPlaceFor);
        when(placeService.updatePlace(testPlaceFor)).thenReturn(testPlaceFor);

        assertThat(placeViewHandler.updatePlace(scanner, testAdminUser.getUserId())).isEqualTo(testPlaceFor);
    }

    @Test
    void shouldReturnException_updateExistPlaceBySimpleUserTest() throws IOException {
        when(userService.getExistUserById(simpleUser.getUserId())).thenReturn(simpleUser);

        assertThatThrownBy(()->placeViewHandler.updatePlace(scanner, simpleUser.getUserId()))
                .isInstanceOf(PlaceViewHandlerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }

    @Test
    void shouldReturnException_updateExistButReservationPlaceByAdminUserTest() throws IOException {
        String strCommand = "8\n12";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.getExistUserById(testAdminUser.getUserId()))
                .thenReturn(testAdminUser);
        when(reservationService.findReservationByPlaceId(testPlaceFor.getPlaceId()))
                .thenReturn(List.of(new Reservation()));

        assertThatThrownBy(()->placeViewHandler.updatePlace(scanner, testAdminUser.getUserId()))
                .isInstanceOf(PlaceViewHandlerException.class)
                .hasMessageContaining("Нельзя обновлять данные по зарезервированному месту/залу!");
    }
}