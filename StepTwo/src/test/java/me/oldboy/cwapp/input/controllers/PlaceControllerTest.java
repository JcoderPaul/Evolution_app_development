package me.oldboy.cwapp.input.controllers;

import me.oldboy.cwapp.exceptions.controllers.PlaceControllerException;
import me.oldboy.cwapp.input.entity.*;
import me.oldboy.cwapp.input.service.PlaceService;
import me.oldboy.cwapp.input.service.ReserveService;
import me.oldboy.cwapp.input.service.UserService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class PlaceControllerTest {

    @Mock
    private PlaceService placeService;
    @Mock
    private ReserveService reservationService;
    @Mock
    private UserService userService;
    @InjectMocks
    private PlaceController placeController;

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
        testAdminUser = new User(3L, "Admin", "1234", Role.ADMIN);
        simpleUser = new User(4L, "User", "1234", Role.USER);
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

    /* Тестируем метод *.createNewPlaceTest() создания рабочих мест и конференц-залов */

    @Test
    void shouldReturnCreatedPlaceId_createNewPlaceTest() {
        String strCommand = "зал\n6";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(placeService.isPlaceExist(any(Species.class), anyInt())).thenReturn(false);
        when(placeService.createPlace(any(Place.class))).thenReturn(testPlaceFor.getPlaceId());

        assertThat(placeController.createNewPlace(scanner, testAdminUser.getUserId())
                .equals(testPlaceFor.getPlaceId()));
    }

    @Test
    void shouldReturnExceptionUserHaveNoPermission_createNewPlaceTest() throws IOException {
        when(userService.findUserById(simpleUser.getUserId())).thenReturn(simpleUser);

        assertThatThrownBy(()->placeController.createNewPlace(scanner, simpleUser.getUserId()))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }

    @Test
    void shouldReturnExceptionCreateDuplicatePlace_createNewPlaceTest() throws IOException {
        String strCommand = "зал\n6";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(placeService.isPlaceExist(any(Species.class), anyInt())).thenReturn(true);

        assertThatThrownBy(() -> placeController.createNewPlace(scanner, testAdminUser.getUserId()))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("Невозможно создать дубликат '" +
                                                testPlaceFor.getSpecies().getStrName() +
                                                "' !");
    }

    /* Тестируем метод *.readPlaceById() чтение рабочего места и конференц-зала */

    @Test
    void shouldReturnPlace_readPlaceByIdTest() {
        when(placeService.findPlaceById(testPlaceFor.getPlaceId())).thenReturn(testPlaceFor);
        assertThat(placeController.readPlaceById(testPlaceFor.getPlaceId())).isEqualTo(testPlaceFor);
    }

    @Test
    void shouldReturnExceptionHaveNoPlaceWithEnterId_readPlaceByIdTest() {
        when(placeService.findPlaceById(testPlaceFor.getPlaceId())).thenReturn(null);
        assertThatThrownBy(() -> placeController.readPlaceById(testPlaceFor.getPlaceId()))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("Конференц-зала / рабочего места с ID: " +
                                                testPlaceFor.getPlaceId() +
                                                " не существует!");
    }

    /* Тестируем метод *.showAllHallsAndWorkplaces() отображение всех рабочих мест и конференц-залов */

    @Test
    void shouldReturnAllPlaceListOnScreen_showAllPlacesTest() throws IOException {
        outToScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outToScreen));

        when(placeService.findAllPlaces()).thenReturn(testPlaceList);
        placeController.showAllPlaces();

        outToScreen.flush();
        String allWrittenLines = new String(outToScreen.toByteArray());

        assertThat(allWrittenLines.contains(showAllPlaces)).isTrue();
    }

    /* Тестируем метод *.updatePlace() обновление данных рабочего места и конференц-зала */

    @Test
    void shouldReturnTrueAdminPermission_updatePlaceTest() {
        String strCommand = "8\n12";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(reservationService.findReservationByPlaceId(testPlaceFor.getPlaceId())).thenReturn(List.of());
        when(placeService.findPlaceById(testPlaceFor.getPlaceId())).thenReturn(testPlaceFor);
        when(placeService.updatePlace(testPlaceFor)).thenReturn(true);

        assertThat(placeController.updatePlace(scanner, testAdminUser.getUserId())).isEqualTo(true);
    }

    @Test
    void shouldReturnExceptionUserHaveNoPermission_updatePlaceTest() throws IOException {
        when(userService.findUserById(simpleUser.getUserId())).thenReturn(simpleUser);

        assertThatThrownBy(() -> placeController.updatePlace(scanner, simpleUser.getUserId()))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }

    @Test
    void shouldReturnExceptionPlaceReserved_updatePlaceTest() throws IOException {
        String strCommand = "8\n12";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testAdminUser.getUserId()))
                .thenReturn(testAdminUser);
        when(reservationService.findReservationByPlaceId(testPlaceFor.getPlaceId()))
                .thenReturn(List.of(new Reservation()));

        assertThatThrownBy(() -> placeController.updatePlace(scanner, testAdminUser.getUserId()))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("Нельзя обновлять данные по зарезервированному месту/залу!");
    }

    /* Тестируем метод *.deletePlace() удаления рабочего места и конференц-зала */

    @Test
    void shouldReturnTrue_deletePlaceTest() {
        String strCommand = "8";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testAdminUser.getUserId())).thenReturn(testAdminUser);
        when(placeService.isPlaceExist(testPlaceFor.getPlaceId())).thenReturn(true);
        when(reservationService.findReservationByPlaceId(testPlaceFor.getPlaceId())).thenReturn(List.of());
        when(placeService.deletePlace(testPlaceFor.getPlaceId())).thenReturn(true);

        assertThat(placeController.deletePlace(scanner, testAdminUser.getUserId())).isTrue();
    }

    @Test
    void shouldReturnExceptionPlaceReserved_deletePlaceTest() throws IOException {
        String strCommand = "8";
        inScanner = new ByteArrayInputStream(strCommand.getBytes());
        scanner = new Scanner(inScanner);

        when(userService.findUserById(testAdminUser.getUserId()))
                .thenReturn(testAdminUser);
        when(placeService.isPlaceExist(testPlaceFor.getPlaceId()))
                .thenReturn(true);
        when(reservationService.findReservationByPlaceId(testPlaceFor.getPlaceId()))
                .thenReturn(List.of(new Reservation()));

        assertThatThrownBy(() -> placeController.deletePlace(scanner, testAdminUser.getUserId()))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("Нельзя удалять зарезервированные места/залы");
    }

    @Test
    void shouldReturnExceptionUserHaveNoPermission_deletePlaceTest() throws IOException {
        when(userService.findUserById(simpleUser.getUserId())).thenReturn(simpleUser);

        assertThatThrownBy(() -> placeController.deletePlace(scanner, simpleUser.getUserId()))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }
}