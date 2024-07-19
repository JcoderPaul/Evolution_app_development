package me.oldboy.cwapp.handlers;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.entity.Species;
import me.oldboy.cwapp.handlers.FreeReservationSlotsHandler;
import me.oldboy.cwapp.services.PlaceService;
import me.oldboy.cwapp.services.ReservationService;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FreeReservationSlotsHandlerTest {

    @Mock
    private ReservationService reservationService;
    @Mock
    private PlaceService placeService;
    @InjectMocks
    private FreeReservationSlotsHandler freeReservationSlotsHandler;
    private List<Reservation> testReserveList;
    private List<Place> testPlaceList;
    private Scanner scanner;
    private ByteArrayOutputStream outScreen;
    private ByteArrayInputStream inScanner;

    @BeforeEach
    public void setUp(){
        testReserveList = new ArrayList<>();
        testPlaceList = new ArrayList<>();
        testReserveList.add(new Reservation(1L, LocalDate.of(2025,8,12),
                                            1L,
                                            3L,
                                            LocalTime.of(12,00),
                                            LocalTime.of(23,50)));
        testReserveList.add(new Reservation(2L,
                                            LocalDate.of(2025,8,12),
                                            2L,
                                            3L,
                                            LocalTime.of(16,00),
                                            LocalTime.of(18,00)));
        testReserveList.add(new Reservation(3L,
                                            LocalDate.of(2025,8,12),
                                            2L,
                                            4L,
                                            LocalTime.of(11,30),
                                            LocalTime.of(12,00)));
        testReserveList.add(new Reservation(4L,
                                            LocalDate.of(2025,8,12),
                                            2L,
                                            4L,
                                            LocalTime.of(13,00),
                                            LocalTime.of(14,00)));
        testReserveList.add(new Reservation(5L,
                                            LocalDate.of(2025,8,12),
                                            2L,
                                            4L,
                                            LocalTime.of(00,00),
                                            LocalTime.of(8,00)));
        testReserveList.add(new Reservation(6L,
                                            LocalDate.of(2025,8,12),
                                            2L,
                                            4L,
                                            LocalTime.of(22,00),
                                            LocalTime.of(23,59)));
        testReserveList.add(new Reservation(5L,
                                            LocalDate.of(2025,8,12),
                                            3L,
                                            3L,
                                            LocalTime.of(5,00),
                                            LocalTime.of(8,00)));
        testReserveList.add(new Reservation(6L,
                                            LocalDate.of(2025,8,12),
                                            3L,
                                            3L,
                                            LocalTime.of(19,00),
                                            LocalTime.of(22,15)));

        testPlaceList.add(new Place(1L, Species.HALL, 1));
        testPlaceList.add(new Place(2L, Species.HALL, 2));
        testPlaceList.add(new Place(3L, Species.HALL, 3));
        testPlaceList.add(new Place(4L, Species.WORKPLACE, 1));
        testPlaceList.add(new Place(5L, Species.WORKPLACE, 2));
        testPlaceList.add(new Place(6L, Species.WORKPLACE, 3));

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void showAllFreeSlotsByDate() throws IOException {
        String dateToScanner = "2025-08-12";
        inScanner = new ByteArrayInputStream(dateToScanner.getBytes());

        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        scanner = new Scanner(inScanner);

        when(reservationService.findReservationByDate(any(LocalDate.class))).thenReturn(testReserveList);
        when(placeService.getAllPlaces()).thenReturn(testPlaceList);

        freeReservationSlotsHandler.showAllFreeSlotsByDate(scanner);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains(
                "Для просмотра данных по свободным слотам введите дату (yyyy-mm-dd): " +
                "\nУ Конференц-зал - 1, доступны свободные временные диапазоны: " +
                "\r\n00:00 - 12:00, 23:50 - 23:59" +
                "\nУ Конференц-зал - 2, доступны свободные временные диапазоны: " +
                "\r\n08:00 - 11:30, 12:00 - 13:00, 14:00 - 16:00, 18:00 - 22:00" +
                "\nУ Конференц-зал - 3, доступны свободные временные диапазоны: " +
                "\r\n00:00 - 05:00, 08:00 - 19:00, 22:15 - 23:59" +
                "\nЕсли в данном списке вы не нашли интересующий вас зал/рабочее место, " +
                "\nзначит на выбранную дату они полностью доступны для бронирования с " +
                "00:00 до 23:59! \nМилости просим!")).isTrue();
    }
}