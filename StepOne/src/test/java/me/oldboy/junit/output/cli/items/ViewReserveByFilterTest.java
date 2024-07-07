package me.oldboy.junit.output.cli.items;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.Place;
import me.oldboy.input.entity.ReserveUnit;
import me.oldboy.input.entity.Slots;
import me.oldboy.input.entity.User;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.cli.items.ViewReserveByFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewReserveByFilterTest {

    private static String place;
    private static ByteArrayInputStream inScanner;
    private static ByteArrayOutputStream outView;
    private static Scanner scanner;
    private static ViewReserveByFilter viewReserveByFilter;
    private static ReserveBase reserveBase;
    private static User testUser;
    private static String testDate;
    private static Place testPlace;
    private static Integer testSlotNumber;
    private static UserBase userBase;
    private static HallBase hallBase;
    private static Slots slots = new Slots();
    private static WorkplaceBase workplaceBase;
    private static ReserveUnit reserveUnit;

    @BeforeEach
    public void startInit(){
        CoworkingContext.getInstance();
        reserveBase = CoworkingContext.getReserveBase();
        hallBase = CoworkingContext.getHallBase();
        workplaceBase = CoworkingContext.getWorkplaceBase();
        viewReserveByFilter = new ViewReserveByFilter();
        userBase = CoworkingContext.getUserBase();
        testUser = userBase.getUsersBase().get("Admin");
    }

    @AfterEach
    public void killAllBase(){
        reserveBase.getAllReserveSlots().clear();
        userBase.getUsersBase()
                .get("Admin")
                .getUserReservedUnitList()
                .clear();
    }

    /* Отображение зарезервированных залов */

    @Test
    void viewEmptyReserveByConcreteHall() throws IOException {
        place = "зал";

        inScanner = new ByteArrayInputStream(place.getBytes());
        outView = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outView));
        scanner = new Scanner(inScanner);
        viewReserveByFilter.viewReserveByConcretePlaces(scanner);
        outView.flush();
        String allWrittenLines = new String(outView.toByteArray());

        assertTrue(allWrittenLines.contains("На данный момент нет ни одного зарезервированного зала!"));
    }

    @Test
    void viewReserveByConcreteHall() throws IOException {
        place = "зал";
        testDate = "2024-09-06";
        testPlace = hallBase.readHall(1);
        testSlotNumber = 10;

        reserveUnit = new ReserveUnit(LocalDate.parse(testDate), testPlace,testSlotNumber);
        reserveBase.reserveSlot(testUser, reserveUnit);
        inScanner = new ByteArrayInputStream(place.getBytes());
        outView = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outView));
        scanner = new Scanner(inScanner);
        viewReserveByFilter.viewReserveByConcretePlaces(scanner);
        outView.flush();
        String allWrittenLines = new String(outView.toByteArray());
        System.out.println(allWrittenLines);

        assertTrue(allWrittenLines.contains(testDate +
                                            ": Конференц-зал № " +
                                            testPlace.getNumber() +
                                            " слот номер: " +
                                            testSlotNumber + " время " +
                                            slots.getFreeSlots().get(testSlotNumber)));
    }

    /* Отображение зарезервированных рабочих мест */

    @Test
    void viewEmptyReserveByConcreteWorkplace() throws IOException {
        place = "место";

        inScanner = new ByteArrayInputStream(place.getBytes());
        outView = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outView));
        scanner = new Scanner(inScanner);
        viewReserveByFilter.viewReserveByConcretePlaces(scanner);
        outView.flush();
        String allWrittenLines = new String(outView.toByteArray());

        assertTrue(allWrittenLines.contains("На данный момент нет ни одного зарезервированного рабочего места!"));
    }

    @Test
    void viewReserveByConcreteWorkplace() throws IOException {
        place = "место";
        testDate = "2025-09-06";
        testPlace = workplaceBase.readWorkPlace(3);
        testSlotNumber = 12;

        reserveUnit = new ReserveUnit(LocalDate.parse(testDate), testPlace, testSlotNumber);
        reserveBase.reserveSlot(testUser, reserveUnit);
        inScanner = new ByteArrayInputStream(place.getBytes());
        outView = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outView));
        scanner = new Scanner(inScanner);
        viewReserveByFilter.viewReserveByConcretePlaces(scanner);
        outView.flush();
        String allWrittenLines = new String(outView.toByteArray());
        System.out.println(allWrittenLines);

        assertTrue(allWrittenLines.contains(testDate +
                                            ": Рабочее место № " +
                                            testPlace.getNumber() +
                                            " слот номер " +
                                            testSlotNumber + " время " +
                                            slots.getFreeSlots().get(testSlotNumber)));
    }


    @Test
    void viewAllReserveByDateWithNoReserve() throws IOException {
        testDate = "2034-09-09";

        inScanner = new ByteArrayInputStream(testDate.getBytes());
        outView = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outView));
        scanner = new Scanner(inScanner);
        viewReserveByFilter.viewAllReserveByDate(scanner);
        outView.flush();
        String allWrittenLines = new String(outView.toByteArray());

        assertTrue(allWrittenLines.contains("На " + testDate + " зарезервированных залов и рабочих мест нет! \n" +
                                            "Милости просим!"));
    }

    @Test
    void viewAllReserveByDateWithExistingReserve() throws IOException {
        place = "зал";
        testDate = "2025-02-05";
        testPlace = hallBase.readHall(2);
        testSlotNumber = 14;

        reserveUnit = new ReserveUnit(LocalDate.parse(testDate), testPlace, testSlotNumber);
        reserveBase.reserveSlot(testUser, reserveUnit);
        inScanner = new ByteArrayInputStream(testDate.getBytes());
        outView = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outView));
        scanner = new Scanner(inScanner);
        viewReserveByFilter.viewAllReserveByDate(scanner);
        outView.flush();
        String allWrittenLines = new String(outView.toByteArray());

        assertTrue(allWrittenLines.contains("На " + testDate + " заняты: \r\n" +
                                            testPlace.getSpecies() + " - " +
                                            testPlace.getNumber() +
                                            ": \r\nНомер слота и время: " +
                        reserveBase.showAllSeparateSlotsByDate(LocalDate.parse(testDate))
                           .get(testPlace)
                           .getReserveSlots()));
    }
}