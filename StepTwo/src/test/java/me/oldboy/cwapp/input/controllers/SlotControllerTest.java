package me.oldboy.cwapp.input.controllers;

import me.oldboy.cwapp.exceptions.controllers.SlotControllerException;
import me.oldboy.cwapp.input.entity.*;
import me.oldboy.cwapp.input.service.ReserveService;
import me.oldboy.cwapp.input.service.SlotService;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SlotControllerTest {

    @Mock
    private SlotService slotService;
    @Mock
    private UserService userService;
    @Mock
    private ReserveService reserveService;
    @InjectMocks
    private SlotController slotController;
    private Scanner scanner;
    private ByteArrayInputStream inScannerSteam;
    private ByteArrayOutputStream outScreenStream;
    private Long testSlotId;
    private Integer testSlotNumber;
    private LocalDate testDate;
    private LocalTime testTimeStart;
    private LocalTime testTimeFinish;
    private User testUser;
    private User testAdmin;
    private Place testPlace;
    private Slot testSlotWithData;
    private Slot testUpdateSlot;
    private List<Slot> testSlotList = new ArrayList<>();
    private String outScreenAllSlots;

    @BeforeEach
    public void setUp(){
        testSlotId = 10L;
        testSlotNumber = 19;
        testAdmin = new User(5L, "User","user", Role.ADMIN);
        testUser = new User(5L, "User","user", Role.USER);
        testPlace = new Place(12L, Species.HALL, 3);
        testDate = LocalDate.of(2028,9,19);
        testTimeStart = LocalTime.of(19,00);
        testTimeFinish = LocalTime.of(20,00);
        testSlotWithData = new Slot(testSlotId, testSlotNumber, testTimeStart, testTimeFinish);

        testUpdateSlot = new Slot(7L, 16, LocalTime.parse("16:00"), LocalTime.parse("17:00"));
        testSlotList.add(testUpdateSlot);
        testSlotList.add(new Slot(8L, 17, LocalTime.parse("17:00"), LocalTime.parse("18:00")));
        testSlotList.add(new Slot(9L, 18, LocalTime.parse("18:00"), LocalTime.parse("19:00")));

        outScreenAllSlots = " Slot [ slotId: 7, slotNumber: 16, time range: 16:00 - 17:00 ]\r\n" +
                            " Slot [ slotId: 8, slotNumber: 17, time range: 17:00 - 18:00 ]\r\n" +
                            " Slot [ slotId: 9, slotNumber: 18, time range: 18:00 - 19:00 ]";

        MockitoAnnotations.openMocks(this);
    }

    /* Тестируем метод *.createNewSlot() условного уровня контроллеров */

    @Test
    void shouldReturnSlotId_createNewSlotTest() {
        String inScannerString = "19\n19:00\n20:00";
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findSlotByNumber(testSlotNumber)).thenReturn(null);
        when(slotService.createSlot(any(Slot.class))).thenReturn(testSlotId);

        assertThat(slotController.createNewSlot(scanner, testAdmin.getUserId())).isEqualTo(testSlotId);
    }

    @Test
    void shouldReturnExceptionUserHaveNoPermission_createNewSlotTest() {
        String inScannerString = "";
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testUser.getUserId())).thenReturn(testUser);

        assertThatThrownBy(() -> slotController.createNewSlot(scanner, testUser.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }

    @Test
    void shouldReturnExceptionSlotNumberIsExist_createNewSlotTest() {
        String inScannerString = "19\n19:00\n20:00";
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findSlotByNumber(testSlotNumber)).thenReturn(testSlotWithData);

        assertThatThrownBy(() -> slotController.createNewSlot(scanner, testAdmin.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Слот с номером " + testSlotNumber + " уже существует");
    }

    @Test
    void shouldReturnExceptionWrongTimeRange_createNewSlotTest() {
        String inScannerString = "19\n20:01\n19:58";
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findSlotByNumber(testSlotNumber)).thenReturn(null);

        assertThatThrownBy(() -> slotController.createNewSlot(scanner, testAdmin.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Время начала диапазона не может быть после его окончания!");
    }

    /* Тестируем метод *.viewAllSlots() условного уровня контроллеров */

    @Test
    void viewAllSlotsTest() throws IOException {
        outScreenStream = new ByteArrayOutputStream(); // Создаем исходящий байтовый поток
        System.setOut(new PrintStream(outScreenStream)); // Переназначаем системный вывод на нужный нам

        when(slotService.findAllSlots()).thenReturn(testSlotList); // Подменяем (имитируем) результат вывода в SlotController
        slotController.viewAllSlots(); // Запускаем интересующий нас метод, в котором сделали подмену

        outScreenStream.flush(); // Сбрасываем буфер в исходящий поток
        String allWrittenLines = new String(outScreenStream.toByteArray()); // Формируем строку из сброшенных данных
        assertThat(allWrittenLines.contains(outScreenAllSlots)).isTrue();
    }

    /* Тестируем метод *.updateSlot() условного уровня контроллеров */

    @Test
    void shouldReturnTrue_updateSlotTest() {
        String newSlotNumber = "163";
        String newStartSlotTime = "16:30";
        String newFinishSlotTime = "17:00";
        String inScannerString = testUpdateSlot.getSlotId() +
                                 "\n" + newSlotNumber +
                                 "\n" + newStartSlotTime +
                                 "\n" + newFinishSlotTime;
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findAllSlots()).thenReturn(testSlotList);
        when(slotService.findSlotByNumber(testUpdateSlot.getSlotNumber())).thenReturn(testUpdateSlot);
        when(slotService.findSlotById(testUpdateSlot.getSlotId())).thenReturn(testUpdateSlot);
        when(slotService.updateSlot(testUpdateSlot)).thenReturn(true);

        assertThat(slotController.updateSlot(scanner, testAdmin.getUserId())).isEqualTo(true);
    }

    @Test
    void shouldReturnExceptionUserHaveNoPermission_updateSlotTest() {
        String inScannerString = "";
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testUser.getUserId())).thenReturn(testUser);

        assertThatThrownBy(() -> slotController.updateSlot(scanner, testUser.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }

    @Test
    void shouldReturnExceptionSlotIsReserved_updateSlotTest() {
        String inScannerString = "7\n163\n16:30\n17:00";
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findAllSlots()).thenReturn(testSlotList);
        when(reserveService.findReservationsBySlotId(testUpdateSlot.getSlotId()))
                .thenReturn(List.of(new Reservation()));

        assertThatThrownBy(() -> slotController.updateSlot(scanner, testAdmin.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Нельзя обновлять данные по зарезервированному слоту!");
    }

    @Test
    void shouldReturnExceptionWrongTimeRange_updateSlotTest() {
        String inScannerString = "7\n163\n17:00\n16:30";
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findAllSlots()).thenReturn(testSlotList);
        when(reserveService.findReservationsBySlotId(testUpdateSlot.getSlotId())).thenReturn(List.of());

        assertThatThrownBy(() -> slotController.updateSlot(scanner, testAdmin.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Время начала диапазона не может быть после его окончания!");
    }

    /* Тестируем метод *.deleteSlot() условного уровня контроллеров */

    @Test
    void shouldReturnTrue_deleteSlotTest() {
        Long deleteSlotId = testUpdateSlot.getSlotId();
        String inScannerString = Long.toString(deleteSlotId);
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findAllSlots()).thenReturn(testSlotList);
        when(reserveService.findReservationsBySlotId(deleteSlotId)).thenReturn(List.of());
        when(slotService.findSlotById(deleteSlotId)).thenReturn(testUpdateSlot);
        when(slotService.deleteSlot(deleteSlotId)).thenReturn(true);

        assertThat(slotController.deleteSlot(scanner, testAdmin.getUserId())).isEqualTo(true);
    }

    @Test
    void shouldReturnExceptionUserHaveNoPermission_deleteSlotTest() {
        Long deleteSlotId = testUpdateSlot.getSlotId();
        String inScannerString = Long.toString(deleteSlotId);
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testUser.getUserId())).thenReturn(testUser);

        assertThatThrownBy(() -> slotController.deleteSlot(scanner, testAdmin.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("У пользователя недостаточно прав!");
    }

    @Test
    void shouldReturnExceptionSlotIsReserved_deleteSlotTest() {
        Long deleteSlotId = testUpdateSlot.getSlotId();
        String inScannerString = Long.toString(deleteSlotId);
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findAllSlots()).thenReturn(testSlotList);
        when(reserveService.findReservationsBySlotId(deleteSlotId)).thenReturn(List.of(new Reservation()));

        assertThatThrownBy(() -> slotController.deleteSlot(scanner, testAdmin.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Нельзя удалять зарезервированный слот!");
    }

    @Test
    void shouldReturnExceptionHaveNoSlotId_deleteSlotTest() {
        Long deleteSlotId = testUpdateSlot.getSlotId();
        String inScannerString = Long.toString(deleteSlotId);
        inScannerSteam = new ByteArrayInputStream(inScannerString.getBytes());
        scanner = new Scanner(inScannerString);

        when(userService.findUserById(testAdmin.getUserId())).thenReturn(testAdmin);
        when(slotService.findAllSlots()).thenReturn(testSlotList);
        when(reserveService.findReservationsBySlotId(deleteSlotId)).thenReturn(List.of());
        when(slotService.findSlotById(deleteSlotId)).thenReturn(null);

        assertThatThrownBy(() -> slotController.deleteSlot(scanner, testAdmin.getUserId()))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Слот для удаления не найден!");
    }
}