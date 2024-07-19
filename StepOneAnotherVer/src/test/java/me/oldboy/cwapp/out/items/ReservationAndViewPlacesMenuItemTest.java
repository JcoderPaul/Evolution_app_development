package me.oldboy.cwapp.out.items;

import me.oldboy.cwapp.handlers.FreeReservationSlotsHandler;
import me.oldboy.cwapp.handlers.PlaceViewHandler;
import me.oldboy.cwapp.handlers.ReservationViewHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ReservationAndViewPlacesMenuItemTest {

    @Mock
    private ReservationViewHandler reservationViewHandler;
    @Mock
    private PlaceViewHandler placeViewHandler;
    @Mock
    private FreeReservationSlotsHandler freeReservationSlotsHandler;
    @InjectMocks
    private ReservationAndViewPlacesMenuItem reservationAndViewPlacesMenuItem;
    private Scanner scanner;
    private Long userId;
    private Long testPlaceId;
    private ByteArrayInputStream inScanner;
    private ByteArrayOutputStream outScreen;

    @BeforeEach
    public void setUp(){
        userId = 1L;
        testPlaceId = 13L;
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnTrue_manageByReservationAndViewPlaceFirstTwoItem() throws IOException {
        String userChoice = "1\n2\n6";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));

        reservationAndViewPlacesMenuItem.manageByReservationAndViewPlace(scanner, userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("Выберите один из пунктов меню: " +
                "\n1 - просмотр списка всех доступных рабочих мест и конференц-залов;" +
                "\n2 - просмотр доступных слотов для бронирования на конкретную дату;" +
                "\n3 - бронирование рабочего места или конференц-зала на определённое время и дату;" +
                "\n4 - отмена бронирования;" +
                "\n5 - просмотр всех бронирований с фильтрацией;" +
                "\n6 - покинуть меню резервирования;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }

    @Test
    void shouldReturnTrue_manageByReservationAndViewPlaceItemThree() throws IOException {
        String userChoice = "3\n1\n3\n2\n6";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));
        when(reservationViewHandler.reservationHall(scanner, userId)).thenReturn(testPlaceId);
        when(reservationViewHandler.reservationWorkplace(scanner, userId)).thenReturn(testPlaceId);
        reservationAndViewPlacesMenuItem.manageByReservationAndViewPlace(scanner, userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("Выберите один из пунктов меню: " +
                "\n1 - просмотр списка всех доступных рабочих мест и конференц-залов;" +
                "\n2 - просмотр доступных слотов для бронирования на конкретную дату;" +
                "\n3 - бронирование рабочего места или конференц-зала на определённое время и дату;" +
                "\n4 - отмена бронирования;" +
                "\n5 - просмотр всех бронирований с фильтрацией;" +
                "\n6 - покинуть меню резервирования;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();

        assertThat(allWrittenLines.contains("Выберите, что планируете резервировать: " +
                "\n1 - Конференц зал." +
                "\n2 - Рабочее место." +
                "\n\nСделайте выбор и нажмите ввод:")).isTrue();
    }

    @Test
    void shouldReturnTrue_manageByReservationAndViewPlaceItemFour() throws IOException {
        String userChoice = "4\n1\n4\n2\n6";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));
        when(reservationViewHandler.deleteHallReservation(scanner, userId)).thenReturn(true);
        when(reservationViewHandler.deleteWorkplaceReservation(scanner, userId)).thenReturn(true);
        reservationAndViewPlacesMenuItem.manageByReservationAndViewPlace(scanner, userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("Выберите один из пунктов меню: " +
                "\n1 - просмотр списка всех доступных рабочих мест и конференц-залов;" +
                "\n2 - просмотр доступных слотов для бронирования на конкретную дату;" +
                "\n3 - бронирование рабочего места или конференц-зала на определённое время и дату;" +
                "\n4 - отмена бронирования;" +
                "\n5 - просмотр всех бронирований с фильтрацией;" +
                "\n6 - покинуть меню резервирования;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();

        assertThat(allWrittenLines.contains("Выберите, с чего планируете снять бронь: " +
                "\n1 - Конференц зал." +
                "\n2 - Рабочее место.\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }

    @Test
    void shouldReturnTrue_manageByReservationAndViewPlaceItemFive() throws IOException {
        String userChoice = "5\n1\n5\n2\n5\n3\n5\n4\n6";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));

        reservationAndViewPlacesMenuItem.manageByReservationAndViewPlace(scanner, userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("Выберите один из пунктов меню: " +
                "\n1 - просмотр списка всех доступных рабочих мест и конференц-залов;" +
                "\n2 - просмотр доступных слотов для бронирования на конкретную дату;" +
                "\n3 - бронирование рабочего места или конференц-зала на определённое время и дату;" +
                "\n4 - отмена бронирования;" +
                "\n5 - просмотр всех бронирований с фильтрацией;" +
                "\n6 - покинуть меню резервирования;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();

        assertThat(allWrittenLines.contains("Выберите в каком формате желаете просмотреть текущие брони: " +
                "\n1 - все без фильтрации;" +
                "\n2 - отфильтровать по дате;" +
                "\n3 - только ваши брони;" +
                "\n4 - по выбранному ресурсу;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }
}