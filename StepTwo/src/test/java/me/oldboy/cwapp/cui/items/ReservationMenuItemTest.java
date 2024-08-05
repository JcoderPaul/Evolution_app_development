package me.oldboy.cwapp.cui.items;

import me.oldboy.cwapp.core.controllers.PlaceController;
import me.oldboy.cwapp.core.controllers.ReserveController;
import me.oldboy.cwapp.core.service.PlaceService;
import me.oldboy.cwapp.core.service.ReserveService;
import me.oldboy.cwapp.core.service.SlotService;
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

class ReservationMenuItemTest {

    @Mock
    private ReserveController reserveController;
    @Mock
    private ReserveService reserveService;
    @Mock
    private PlaceController placeController;
    @Mock
    private PlaceService placeService;
    @Mock
    private SlotService slotService;
    @InjectMocks
    private ReservationMenuItem reservationMenuItem;
    private Scanner scanner;
    private Long userId;
    private Long testPlaceId;
    private ByteArrayInputStream inScanner;
    private ByteArrayOutputStream outScreen;

    private String reservationCrudMenu = "Выберите один из пунктов меню: " +
            "\n1 - просмотр списка всех доступных рабочих мест и конференц-залов;" +
            "\n2 - просмотр доступных слотов для бронирования на конкретную дату;" +
            "\n3 - бронирование рабочего места или конференц-зала на определённое время и дату;" +
            "\n4 - отмена бронирования;" +
            "\n5 - просмотр всех бронирований с фильтрацией;" +
            "\n6 - покинуть меню резервирования;\n\n" +
            "Сделайте выбор и нажмите ввод: ";

    @BeforeEach
    public void setUp(){
        userId = 1L;
        testPlaceId = 13L;
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFirstToFourthMenuItem_manageByReservationAndViewPlaceTest() throws IOException {
        String userChoice = "1\n2\n2029-07-28\n3\n4\n6";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));
        when(reserveController.reservationPlace(scanner, userId)).thenReturn(testPlaceId);
        when(reserveController.deletePlaceReservation(scanner, userId)).thenReturn(true);
        reservationMenuItem.manageByReservationAndViewPlace(scanner, userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(reservationCrudMenu)).isTrue();
    }

    @Test
    void testFifthMenuItem_manageByReservationAndViewPlaceItemThree() throws IOException {
        String userChoice = "5\n1\n2\n2029-07-28\n3\n4\n1\n6";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));

        reservationMenuItem.manageByReservationAndViewPlace(scanner, userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(reservationCrudMenu)).isTrue();
    }
}