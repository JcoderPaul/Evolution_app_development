package me.oldboy.junit.output.cli.items;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.controllers.CoworkingSpaceController;
import me.oldboy.input.entity.ReserveUnit;
import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.HallBaseException;
import me.oldboy.input.exeptions.SpaceControllerException;
import me.oldboy.input.exeptions.WorkplaceBaseException;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.cli.items.ReserveCrudOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ReserveCrudOperationTest {

    private static ReserveCrudOperation reserveCrudOperation;
    private static Scanner scanner;
    private static ByteArrayInputStream inToScanner;
    private static CoworkingSpaceController coworkingSpaceController;
    private static ReserveBase reserveBase;
    private static UserBase userBase;
    private static HallBase hallBase;
    private static WorkplaceBase workplaceBase;
    private static User user;
    private static Integer testPlaceNumber;
    private static String placeSpecies;
    private static String reserveDate;
    private static ByteArrayOutputStream outStringData;
    private static String slotNumberFor;

    @BeforeEach
    public void startInit(){
        CoworkingContext.getInstance();
        hallBase = CoworkingContext.getHallBase();
        hallBase.initHallBase();
        workplaceBase = CoworkingContext.getWorkplaceBase();
        workplaceBase.initPlaceBase();
        reserveBase = CoworkingContext.getReserveBase();
        userBase = CoworkingContext.getUserBase();
        coworkingSpaceController = CoworkingContext.getCoworkingSpaceController();
        reserveCrudOperation = new ReserveCrudOperation();
        user = CoworkingContext.getUserBase()
                .login("Admin")
                .get();
    }

    @AfterEach
    public void killBaseAfterEach(){
        reserveBase.getAllReserveSlots().clear();
        userBase.getUsersBase()
                .get("Admin")
                .getUserReservedUnitList()
                .clear();
    }

    /* Тестируем удаление резервирования 'зала' на конкретную дату */

    @Test
    @DisplayName("1 - First assert should return true - create new reserve on main reserve base, " +
                 "second assert should return false - create new reserve on user personal reserve base, " +
                 "third assert should return true - remove just now create place and slot reservation, " +
                 "fourth assert should return false - personal user reservation base nave no contains " +
                 "just now create and delete reserve")
    void removeAlreadyExistingHallReserveFromSlotsByDate() {

        /* Изначально все базы зарезервированных мест, залов, слотов пусты - вносим данные, проверяем, удаляем, проверяем снова */

        placeSpecies = "зал"; // Резервируемый зал
        testPlaceNumber = 3; // Зал с номером 1
        reserveDate = "2024-06-08"; // На дату
        slotNumberFor = "10"; // Номер слота 10
        ReserveUnit reserveUnit = new ReserveUnit(LocalDate.parse(reserveDate),
                                                  hallBase.readHall(testPlaceNumber),
                                                  Integer.parseInt(slotNumberFor));
        assertTrue(reserveBase.reserveSlot(user, reserveUnit)); // Резервируем слот
        assertFalse(userBase.getUsersBase().get(user.getLogin()).getUserReservedUnitList().isEmpty()); // Проверяем индивидуальную базу резервирования user-a

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        reserveCrudOperation.removeReserveFromSlot(scanner, user); // Запускаем метод удаления зарезервированного слота

        assertTrue(reserveBase.getAllReserveSlots()
                              .get(LocalDate.parse(reserveDate))
                              .isEmpty()); // Проверяем есть ли удаленное резервирование в общей базе резервирования
        assertFalse(userBase.getUsersBase()
                            .get(user.getLogin())
                            .getUserReservedUnitList()
                            .containsKey(reserveUnit.hashCode())); // Проверяем есть ли удаленное резервирование в индивидуальной базе user-a

    }

    @Test
    @DisplayName("2 - Should return exception - can not delete non-existent reservation")
    void removeNotExistingHallReservationExceptionFromSlotsByDate() {

        /* Изначально все базы зарезервированных мест, залов, слотов пусты - удаляем не существующее резервирование */

        placeSpecies = "зал"; // Резервируемый зал
        testPlaceNumber = 1; // С номером
        reserveDate = "2024-08-08"; // На дату
        slotNumberFor = "10"; // Номер слота

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        assertThrows(SpaceControllerException.class,
                     ()->reserveCrudOperation.removeReserveFromSlot(scanner, user)); // Запускаем метод удаления не зарезервированного слота
    }

    /* Тестируем удаление резервирования 'рабочего места' на конкретную дату */

    @Test
    @DisplayName("3 - First assert should return true - create new reserve on main reserve base, " +
                 "second assert should return false - create new reserve on user personal reserve base, " +
                 "third assert should return true - remove just now create place and slot reservation, " +
                 "fourth assert should return false - personal user reservation base nave no contains " +
                 "just now create and delete reserve")
    void removeAlreadyExistingWorkplaceReserveFromSlotsByDate() {

        /* Изначально все базы зарезервированных мест, залов, слотов пусты - вносим данные, проверяем, удаляем, проверяем снова */

        placeSpecies = "место"; // Резервируемое рабочее место
        testPlaceNumber = 6; // Рабочее место с номером
        reserveDate = "2024-09-08"; // На дату
        slotNumberFor = "12"; // Номер слота
        ReserveUnit reserveUnit = new ReserveUnit(LocalDate.parse(reserveDate),
                                                  workplaceBase.readWorkPlace(testPlaceNumber),
                                                  Integer.parseInt(slotNumberFor));
        assertTrue(reserveBase.reserveSlot(user, reserveUnit)); // Резервируем слот
        assertFalse(userBase.getUsersBase().get(user.getLogin()).getUserReservedUnitList().isEmpty()); // Проверяем индивидуальную базу резервирования user-a

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        reserveCrudOperation.removeReserveFromSlot(scanner, user); // Запускаем метод удаления зарезервированного слота

        assertTrue(reserveBase.getAllReserveSlots()
                              .get(LocalDate.parse(reserveDate))
                              .isEmpty()); // Проверяем есть ли удаленное резервирование в общей базе резервирования
        assertFalse(userBase.getUsersBase()
                           .get(user.getLogin())
                           .getUserReservedUnitList()
                           .containsKey(reserveUnit.hashCode())); // Проверяем есть ли удаленное резервирование в индивидуальной базе user-a
    }

    @Test
    @DisplayName("4 - Should return exception - can not delete non-existent reservation")
    void removeNotExistingWorkplaceReservationExceptionFromSlotsByDate() {

        /* Изначально все базы зарезервированных мест, залов, слотов пусты - удаляем не существующее резервирование */

        placeSpecies = "место"; // Резервируемое рабочее место
        testPlaceNumber = 1; // С номером
        reserveDate = "2024-08-08"; // На дату
        slotNumberFor = "13"; // Номер слота

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        assertThrows(SpaceControllerException.class,
                     ()->reserveCrudOperation.removeReserveFromSlot(scanner, user)); // Запускаем метод удаления не зарезервированного слота
    }

    /* Тестируем резервирование 'зала' на конкретную дату */

    @Test
    @DisplayName("5 - Should return false - reserve is added, and reserve bases is not empty")
    void reserveHallToEnterDateAndSlot() {
        placeSpecies = "зал"; // Резервируемый зал
        testPlaceNumber = 1; // Зал с номером - проверка на наличие зала с таким номером проводится (такой зал есть)
        reserveDate = "2025-09-08"; // На дату
        slotNumberFor = "16"; // Номер слота - проверки на наличие слота не проводится, допускаем, что пользователь внимателен
        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        reserveCrudOperation.reservePlaceToEnterDateAndSlot(scanner, user); // Резервируем зал согласно параметрам

        assertFalse(userBase.getUsersBase()
                            .get(user.getLogin())
                            .getUserReservedUnitList().isEmpty()); // Общая база резервирования не пуста
        assertFalse(reserveBase.getAllReserveUnit().isEmpty()); // Индивидуальная база User-ов резервирования не пуста
    }

    @Test
    @DisplayName("6 - Should return exception - reserve not existing hall")
    void reserveNotExistingHallToEnterDateAndSlot() {
        placeSpecies = "зал"; // Резервируемый зал
        testPlaceNumber = 8; // Зал с номером - проверка на наличие зала с таким номером проводится (его нет)
        reserveDate = "2025-09-08"; // На дату
        slotNumberFor = "16"; // Номер слота - проверки на наличие слота не проводится, допускаем, что пользователь внимателен

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        assertThrows(HallBaseException.class,
                     ()->reserveCrudOperation.reservePlaceToEnterDateAndSlot(scanner, user)); // Резервируем не существующий зал
    }

    @Test
    @DisplayName("7 - Should return exception - double reservation is not impossible")
    void tryDoubleReservationExistingHallToEnterDateAndSlot() {
        placeSpecies = "зал"; // Резервируемый зал
        testPlaceNumber = 2; // Зал с номером - проверка на наличие зала с таким номером проводится (таковой есть)
        reserveDate = "2025-09-08"; // На дату
        slotNumberFor = "16"; // Номер слота - проверки на наличие слота не проводится, допускаем, что пользователь внимателен

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        reserveCrudOperation.reservePlaceToEnterDateAndSlot(scanner, user); // Первичное резервирование

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);
        assertThrows(SpaceControllerException.class,
                     ()->reserveCrudOperation.reservePlaceToEnterDateAndSlot(scanner, user)); // Повторное резервирование
    }

    /* Тестируем резервирование 'рабочего места' на конкретную дату */

    @Test
    @DisplayName("8 - Should return false - reserve is added, and reserve bases is not empty")
    void reserveWorkplaceToEnterDateAndSlot() {
        placeSpecies = "место"; // Резервируемое рабочее место
        testPlaceNumber = 5; // Рабочее место с таким номером есть - проверка проводится
        reserveDate = "2025-09-08"; // На дату
        slotNumberFor = "18"; // Номер слота - проверки на наличие слота не проводится, допускаем, что пользователь внимателен

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        reserveCrudOperation.reservePlaceToEnterDateAndSlot(scanner, user); // Резервируем зал согласно параметрам
        assertFalse(userBase.getUsersBase()
                            .get(user.getLogin())
                            .getUserReservedUnitList().isEmpty()); // Общая база резервирования не пуста
        assertFalse(reserveBase.getAllReserveUnit().isEmpty()); // Индивидуальная база User-ов резервирования не пуста
    }

    @Test
    @DisplayName("9 - Should return exception - reserve not existing workplace")
    void reserveNonExistingWorkplaceToEnterDateAndSlot() {
        placeSpecies = "место"; // Резервируемое рабочее место
        testPlaceNumber = 35; // Рабочее место с таким номером не существует
        reserveDate = "2025-09-08"; // На дату
        slotNumberFor = "18"; // Номер слота - проверки на наличие слота не проводится, допускаем, что пользователь внимателен

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        assertThrows(WorkplaceBaseException.class,
                     ()->reserveCrudOperation.reservePlaceToEnterDateAndSlot(scanner, user)); // Резервируем не существующее рабочее место
    }

    @Test
    @DisplayName("10 - Should return exception - double reservation is not impossible")
    void tryDoubleReservationExistingWorkplaceToEnterDateAndSlot() {
        placeSpecies = "место"; // Резервируемое рабочее место
        testPlaceNumber = 8; // Рабочее место с номером - проверка на наличие зала с таким номером проводится
        reserveDate = "2026-09-08"; // На дату
        slotNumberFor = "13"; // Номер слота - проверки на наличие слота не проводится, допускаем, что пользователь внимателен

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);

        reserveCrudOperation.reservePlaceToEnterDateAndSlot(scanner, user); // Первичное резервирование

        scanner = fluxCapacitor(placeSpecies, testPlaceNumber, reserveDate, slotNumberFor);
        assertThrows(SpaceControllerException.class,
                ()->reserveCrudOperation.reservePlaceToEnterDateAndSlot(scanner, user)); // Повторное резервирование
    }

    private Scanner fluxCapacitor(String placeSpecies, Integer testPlaceNumber, String date, String slotNumberFor) {
        String makeCommandLine = placeSpecies + " " + testPlaceNumber +
                "\n" + reserveDate +
                "\n" + slotNumberFor; // Формируем строку подменяющую ввод с клавиатуры набор команд
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes()); // Отправляем во входящий байт поток
        return new Scanner(inToScanner); // Передаем поток в сканер
    }
}