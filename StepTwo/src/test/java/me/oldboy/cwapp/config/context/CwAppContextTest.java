package me.oldboy.cwapp.config.context;

import me.oldboy.cwapp.config.connection.ConnectionManager;
import me.oldboy.cwapp.config.liquibase.LiquibaseManager;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
class CwAppContextTest {

    private static Connection connection;
    private static CwAppContext cwAppContext;
    private static LiquibaseManager liquibaseManager = LiquibaseManager.getInstance();
    private ByteArrayInputStream inScanner;
    private Long userAdminId = 1L;
    private Scanner scanner;
    private ByteArrayOutputStream outToScreen;
    private String showAllPlaces = "ID - 1 вид: 'Конференц-зал' номер: 1\r\n" +
                                   "ID - 2 вид: 'Конференц-зал' номер: 2\r\n" +
                                   "ID - 3 вид: 'Конференц-зал' номер: 3\r\n" +
                                   "ID - 4 вид: 'Рабочее место' номер: 1\r\n" +
                                   "ID - 5 вид: 'Рабочее место' номер: 2\r\n" +
                                   "ID - 6 вид: 'Рабочее место' номер: 3\r\n" +
                                   "ID - 7 вид: 'Рабочее место' номер: 4\r\n" +
                                   "ID - 8 вид: 'Рабочее место' номер: 5\r\n" +
                                   "ID - 9 вид: 'Рабочее место' номер: 6";
    private String showAllReservation =
            "Бронь ID - 1 на 2029-07-28 зарезервирован(о): Place [placeId: 1, species: HALL, placeNumber - 1] " +
                    "слот:  Slot [ slotId: 1, slotNumber: 10, time range: 10:00 - 11:00 ] " +
                    "принадлежит: User [userId - 1, login: 'Admin', password: '1234', role: ADMIN]\r\n" +
            "Бронь ID - 2 на 2029-07-28 зарезервирован(о): Place [placeId: 2, species: HALL, placeNumber - 2] " +
                    "слот:  Slot [ slotId: 1, slotNumber: 10, time range: 10:00 - 11:00 ] " +
                    "принадлежит: User [userId - 1, login: 'Admin', password: '1234', role: ADMIN]\r\n" +
            "Бронь ID - 3 на 2029-07-28 зарезервирован(о): Place [placeId: 4, species: WORKPLACE, placeNumber - 1] " +
                    "слот:  Slot [ slotId: 3, slotNumber: 12, time range: 12:00 - 13:00 ] " +
                    "принадлежит: User [userId - 1, login: 'Admin', password: '1234', role: ADMIN]\r\n" +
            "Бронь ID - 4 на 2029-07-28 зарезервирован(о): Place [placeId: 5, species: WORKPLACE, placeNumber - 2] " +
                    "слот:  Slot [ slotId: 6, slotNumber: 15, time range: 15:00 - 16:00 ] " +
                    "принадлежит: User [userId - 2, login: 'User', password: '1234', role: USER]\r\n" +
            "Бронь ID - 5 на 2029-07-29 зарезервирован(о): Place [placeId: 5, species: WORKPLACE, placeNumber - 2] " +
                    "слот:  Slot [ slotId: 6, slotNumber: 15, time range: 15:00 - 16:00 ] " +
                    "принадлежит: User [userId - 2, login: 'User', password: '1234', role: USER]\r\n" +
            "Бронь ID - 6 на 2029-07-29 зарезервирован(о): Place [placeId: 1, species: HALL, placeNumber - 1] " +
                    "слот:  Slot [ slotId: 6, slotNumber: 15, time range: 15:00 - 16:00 ] " +
                    "принадлежит: User [userId - 3, login: 'UserTwo', password: '1234', role: USER]\r\n" +
            "Бронь ID - 7 на 2029-07-29 зарезервирован(о): Place [placeId: 2, species: HALL, placeNumber - 2] " +
                    "слот:  Slot [ slotId: 6, slotNumber: 15, time range: 15:00 - 16:00 ] " +
                    "принадлежит: User [userId - 3, login: 'UserTwo', password: '1234', role: USER]\r\n" +
            "Бронь ID - 8 на 2029-07-29 зарезервирован(о): Place [placeId: 9, species: WORKPLACE, placeNumber - 6] " +
                    "слот:  Slot [ slotId: 6, slotNumber: 15, time range: 15:00 - 16:00 ] " +
                    "принадлежит: User [userId - 3, login: 'UserTwo', password: '1234', role: USER]";
    private String outScreenAllSlots =
            " Slot [ slotId: 1, slotNumber: 10, time range: 10:00 - 11:00 ]\r\n" +
            " Slot [ slotId: 2, slotNumber: 11, time range: 11:00 - 12:00 ]\r\n" +
            " Slot [ slotId: 3, slotNumber: 12, time range: 12:00 - 13:00 ]\r\n" +
            " Slot [ slotId: 4, slotNumber: 13, time range: 13:00 - 14:00 ]\r\n" +
            " Slot [ slotId: 5, slotNumber: 14, time range: 14:00 - 15:00 ]\r\n" +
            " Slot [ slotId: 6, slotNumber: 15, time range: 15:00 - 16:00 ]\r\n" +
            " Slot [ slotId: 7, slotNumber: 16, time range: 16:00 - 17:00 ]\r\n" +
            " Slot [ slotId: 8, slotNumber: 17, time range: 17:00 - 18:00 ]\r\n" +
            " Slot [ slotId: 9, slotNumber: 18, time range: 18:00 - 19:00 ]";

    /* Предварительная настойка */

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:latest")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    @BeforeAll
    public static void startTestContainer() {
        postgresContainer.start();
        connection = ConnectionManager.getTestBaseConnection(postgresContainer.getJdbcUrl(),
                                                             postgresContainer.getUsername(),
                                                             postgresContainer.getPassword()
        );
        liquibaseManager.migrationsStart(connection);
        cwAppContext = CwAppContext.getInstance(connection);
    }

    @AfterAll
    public static void stopTestContainer(){
        liquibaseManager.rollbackCreatedTables(connection);
        postgresContainer.stop();
    }

    /* Тестовая часть */

    @Test
    void getInstanceTest() {
        assertThat(cwAppContext.getClass().getDeclaredFields().length)
                .isEqualTo(13);
    }

    @Test
    void getPlaceRepositoryTest() {
        Integer baseSize = cwAppContext.getPlaceRepository().findAllPlaces().size();
        assertThat(baseSize).isEqualTo(9);
    }

    @Test
    void getUserRepositoryTest() {
        Integer baseSize = cwAppContext.getUserRepository().findAllUsers().size();
        assertThat(baseSize).isEqualTo(3);
    }

    @Test
    void getSlotRepositoryTest() {
        Integer baseSize = cwAppContext.getSlotRepository().findAllSlots().size();
        assertThat(baseSize).isEqualTo(9);
    }

    @Test
    void getReservationRepositoryTest() {
        Integer baseSize = cwAppContext.getReservationRepository().findAllReservation().get().size();
        assertThat(baseSize).isEqualTo(8);
    }

    @Test
    void getPlaceServiceTest() {
        Integer baseSize = cwAppContext.getPlaceService().findAllPlaces().size();
        assertThat(baseSize).isEqualTo(9);
    }

    @Test
    void getReservationServiceTest() {
        Integer baseSize = cwAppContext.getReservationService().findAllReservation().size();
        assertThat(baseSize).isEqualTo(8);
    }

    @Test
    void getUserServiceTest() {
        Integer baseSize = cwAppContext.getUserService().findAllUser().size();
        assertThat(baseSize).isEqualTo(3);
    }

    @Test
    void getSlotServiceTest() {
        Integer baseSize = cwAppContext.getSlotService().findAllSlots().size();
        assertThat(baseSize).isEqualTo(9);
    }
    @Test
    void getPlaceControllerTest() throws IOException {
        outToScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outToScreen));

        cwAppContext.getPlaceController().showAllPlaces();

        outToScreen.flush();
        String allWrittenLines = new String(outToScreen.toByteArray());

        assertThat(allWrittenLines.contains(showAllPlaces)).isTrue();
    }

    @Test
    void getReservationControllerTest() throws IOException {
        outToScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outToScreen));

        cwAppContext.getReserveController().showAllReservation();

        outToScreen.flush();
        String allWrittenLines = new String(outToScreen.toByteArray());
        assertThat(allWrittenLines.contains(showAllReservation)).isTrue();
    }

    @Test
    void getUserControllerTest() {
        String loginPasswordLine = "Admin\n1234";
        inScanner = new ByteArrayInputStream(loginPasswordLine.getBytes());
        scanner = new Scanner(inScanner);

        Long mayBeUserId = cwAppContext.getUserController().loginUser(scanner);
        assertThat(mayBeUserId).isEqualTo(1L);
    }

    @Test
    void getSlotControllerTest() throws IOException {
        outToScreen = new ByteArrayOutputStream(); // Создаем исходящий байтовый поток
        System.setOut(new PrintStream(outToScreen)); // Переназначаем системный вывод на нужный нам

        cwAppContext.getSlotController().viewAllSlots(); // Запускаем интересующий нас метод, в котором сделали подмену

        outToScreen.flush(); // Сбрасываем буфер в исходящий поток
        String allWrittenLines = new String(outToScreen.toByteArray()); // Формируем строку из сброшенных данных
        assertThat(allWrittenLines.contains(outScreenAllSlots)).isTrue();
    }
}