package me.oldboy.cwapp.context;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.service_exception.ReservationServiceException;
import me.oldboy.cwapp.services.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CwAppContextTest {

    private static CwAppContext cwAppContext;
    private ByteArrayInputStream inScanner;
    private ByteArrayOutputStream outScreen;
    private Scanner scanner;

    @BeforeEach
    public void setUp(){
        cwAppContext = CwAppContext.getInstance();
    }

    @AfterEach
    public void getBaseToStart(){
        cwAppContext.getReservationBase().getReservationBase().clear();
    }

    @Test
    void getInstance() {
        assertThat(cwAppContext.getClass().getDeclaredFields().length).isEqualTo(12);
    }

    @Test
    void getPlaceBase() {
        Integer baseSize = cwAppContext.getPlaceBase().getAllPlaceBase().size();
        cwAppContext.getPlaceBase().delete(3L);
        assertThat(cwAppContext.getPlaceBase().getAllPlaceBase().size()).isEqualTo(baseSize - 1);
    }

    @Test
    void getUserBase() {
        Integer baseSize = cwAppContext.getUserBase().getUserBase().size();
        cwAppContext.getUserBase().delete(2L);
        assertThat(cwAppContext.getUserBase().getUserBase().size()).isEqualTo(baseSize - 1);
    }

    @Test
    void getReservationBase() {
        cwAppContext.getReservationBase().create(new Reservation());
        assertThat(cwAppContext.getReservationBase()
                               .getReservationBase()
                               .size())
                .isEqualTo(1);
    }

    @Test
    void getPlaceService() {
        Long testId = cwAppContext.getPlaceService().addNewPlace(new Place());
        assertThat(cwAppContext.getPlaceService()
                               .isPlaceExist(testId))
                .isTrue();
    }

    @Test
    void getReservationService() {
        Long testId = cwAppContext.getReservationService().createReservation(new Reservation());
        assertThat(cwAppContext.getReservationService()
                               .findReservationById(testId)
                               .getReservationId())
                .isEqualTo(testId);
    }

    @Test
    void getUserService() {
        Integer sizeOfBase = cwAppContext.getUserBase().getUserBase().size();

        cwAppContext.getUserService().registration(new User());

        assertThat(cwAppContext.getUserBase().getUserBase().size()).isEqualTo(sizeOfBase + 1);
    }

    @Test
    void getUserAuthenticationHandler() {
        String loginAndPassString = "UserTest\n1234";
        inScanner = new ByteArrayInputStream(loginAndPassString.getBytes());
        scanner = new Scanner(inScanner);

        Boolean isReg = cwAppContext.getUserAuthenticationHandler().registrationUser(scanner);
        assertThat(isReg).isTrue();
    }

    @Test
    void getReservationViewHandler() throws IOException {
        assertThatThrownBy(()->cwAppContext.getReservationViewHandler().showAllReservation())
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("База броней пуста / ошибка связи с БД!");
    }

    @Test
    void getPlaceViewHandler() throws IOException {
        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        cwAppContext.getPlaceViewHandler().showAllHallsAndWorkplaces();

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("ID - 1 вид: 'Конференц-зал' номер: 1" +
                "\r\nID - 2 вид: 'Конференц-зал' номер: 2" +
                "\r\nID - 3 вид: 'Конференц-зал' номер: 3" +
                "\r\nID - 4 вид: 'Рабочее место' номер: 1" +
                "\r\nID - 5 вид: 'Рабочее место' номер: 2" +
                "\r\nID - 6 вид: 'Рабочее место' номер: 3")).isTrue();
    }

    @Test
    void getFreeReservationSlotsHandler() throws IOException {
        String dateString = "1999-09-09";
        inScanner = new ByteArrayInputStream(dateString.getBytes());
        scanner = new Scanner(inScanner);

        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        cwAppContext.getFreeReservationSlotsHandler().showAllFreeSlotsByDate(scanner);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("\nЕсли в данном списке вы не нашли интересующий вас зал/рабочее место, " +
                "\nзначит на выбранную дату они полностью доступны для бронирования с " +
                "00:00 до 23:59! \nМилости просим!")).isTrue();
    }
}