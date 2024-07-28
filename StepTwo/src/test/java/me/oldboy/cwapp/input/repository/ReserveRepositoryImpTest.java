package me.oldboy.cwapp.input.repository;

import me.oldboy.cwapp.config.ConnectionManager;
import me.oldboy.cwapp.config.liquibase.LiquibaseManager;
import me.oldboy.cwapp.input.entity.Place;
import me.oldboy.cwapp.input.entity.Reservation;
import me.oldboy.cwapp.input.entity.Slot;
import me.oldboy.cwapp.input.entity.User;
import me.oldboy.cwapp.input.repository.crud.PlaceRepository;
import me.oldboy.cwapp.input.repository.crud.ReservationRepository;
import me.oldboy.cwapp.input.repository.crud.SlotRepository;
import me.oldboy.cwapp.input.repository.crud.UserRepository;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static me.oldboy.cwapp.input.entity.Species.HALL;
import static me.oldboy.cwapp.input.entity.Species.WORKPLACE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/* Аннотация говорит, что тестирование методов класса идет через Docker тест-контейнер */
@Testcontainers
class ReserveRepositoryImpTest {

    private ReservationRepository reservationRepository;
    private PlaceRepository placeRepository;
    private UserRepository userRepository;
    private SlotRepository slotRepository;
    private Connection connection;
    private LiquibaseManager liquibaseManager = LiquibaseManager.getInstance();

    /* Делаем тестовый контейнер */

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:latest").withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    /* Настраиваем состояние контейнера и БД, до и после каждого / всех тестов */

    @BeforeAll
    public static void startTestContainer() {
        postgresContainer.start();
    }

    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        connection = ConnectionManager.getTestBaseConnection(postgresContainer.getJdbcUrl(),
                                                             postgresContainer.getUsername(),
                                                             postgresContainer.getPassword()
        );
        liquibaseManager.migrationsStart(connection);

        placeRepository = new PlaceRepositoryImpl(connection);
        userRepository = new UserRepositoryImpl(connection);
        slotRepository = new SlotRepositoryImpl(connection);

        reservationRepository =
                new ReserveRepositoryImp(connection, placeRepository, userRepository, slotRepository);
    }

    @AfterEach
    public void resetTestBase(){
        liquibaseManager.rollbackCreatedTables(connection);
    }

    @AfterAll
    public static void stopTestContainer(){
        postgresContainer.stop();
    }

    /* Основные тесты для реализаций методов PlaceRepository */

    @Test
    @DisplayName("1 - Should return creation optional reservation")
    void shouldReturnNewOptionalReservation_createReservationTest() {
        LocalDate testReserveDate = LocalDate.parse("2025-05-14");
        Long testUserId = 1L;
        User testUser = userRepository.findUserById(testUserId).get();
        Long testPlaceId = 2L;
        Place testPlace = placeRepository.findPlaceById(testPlaceId).get();
        Long testSlotId = 3L;
        Slot testSlot = slotRepository.findSlotById(testSlotId).get();

        Reservation newReservation = new Reservation(testReserveDate, testUser, testPlace, testSlot);

        Integer sizeOfReservationBaseBefore = reservationRepository.findAllReservation().get().size();
        Optional<Reservation> mayBeReservation = reservationRepository.createReservation(newReservation);
        Integer sizeOfReservationBaseAfter = reservationRepository.findAllReservation().get().size();

        assertThat(mayBeReservation.isPresent()).isTrue();
        assertThat(sizeOfReservationBaseAfter).isGreaterThan(sizeOfReservationBaseBefore);

        assertAll(
                () -> assertThat(testReserveDate).isEqualTo(mayBeReservation.get().getReserveDate()),
                () -> assertThat(testUserId).isEqualTo(mayBeReservation.get().getUser().getUserId()),
                () -> assertThat(testPlaceId).isEqualTo(mayBeReservation.get().getPlace().getPlaceId()),
                () -> assertThat(testSlotId).isEqualTo(mayBeReservation.get().getSlot().getSlotId())
        );
    }

    /* Тесты метода *.findReservationById() */

    @Test
    @DisplayName("2 - Should return Optional reserve find it by ID")
    void shouldReturnTrueOptionalReserve_findReservationByIdTest() {
        Long testReservationId = 1L; // В данном случае ID брони, совпадает с ID user-a, места и слота
        Optional<Reservation> findReservation = reservationRepository.findReservationById(testReservationId);

        assertThat(findReservation.isPresent()).isTrue();

        /* Поскольку в данном тесте ID брони, пользователя, места, слота в тестовой БД совпадает применяем ее */
        assertAll(
                () -> assertThat(findReservation.get().getReserveId())
                        .isEqualTo(testReservationId),
                () -> assertThat(findReservation.get().getReserveDate())
                        .isEqualTo(LocalDate.parse("2029-07-28")),
                () -> assertThat(findReservation.get().getUser().getUserId())
                        .isEqualTo(testReservationId),
                () -> assertThat(findReservation.get().getPlace().getPlaceId())
                        .isEqualTo(testReservationId),
                () -> assertThat(findReservation.get().getSlot().getSlotId())
                        .isEqualTo(testReservationId)
        );
    }

    @Test
    @DisplayName("3 - Should return Optional null/false to try find non existent reservation by ID")
    void shouldReturnFalseOptionalNull_findNonExistentReservationByIdTest() {
        Optional<Reservation> findNonExistentReserve =
                reservationRepository.findReservationById(18L);

        assertThat(findNonExistentReserve.isPresent()).isFalse();
    }

    /* Тесты метода *.findAllReservation() */

    @Test
    @DisplayName("4 - Should return optional list of reservation")
    void shouldReturnListOfReservation_findAllReservationTest() {
        Optional<List<Reservation>> reservationList = reservationRepository.findAllReservation();

        assertThat(reservationList.isPresent()).isTrue();
        assertThat(reservationList.get().size()).isEqualTo(8);

        List<Reservation> takeThisList = reservationList.get();
        Integer sizeOfThisList = takeThisList.size();

        assertAll(
                () -> assertThat(takeThisList.get(0).getReserveDate())
                        .isEqualTo(LocalDate.parse("2029-07-28")),
                () -> assertThat(takeThisList.get(sizeOfThisList - 1).getReserveDate())
                        .isEqualTo(LocalDate.parse("2029-07-29"))
        );
    }

    /* Тесты метода *.findReservationByDate() */

    @Test
    @DisplayName("5 - Should return optional reservation list find it by date")
    void shouldReturnTrueAndOptionalReservationList_findReservationByDateTest() {
        String testDate = "2029-07-28";
        Optional<List<Reservation>> reservationList =
                reservationRepository.findReservationByDate(LocalDate.parse(testDate));

        assertThat(reservationList.isPresent()).isTrue();
        assertThat(reservationList.get().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("6 - Should return empty collection then try find reservation by free date")
    void shouldReturnEmptyCollection_findReservationByDateTest() {
        String testDate = "2005-06-28";
        Optional<List<Reservation>> reservationList =
                reservationRepository.findReservationByDate(LocalDate.parse(testDate));

        assertThat(reservationList.get().size()).isEqualTo(0);
    }

    /* Тесты метода *.findReservationByPlaceId() */

    @Test
    @DisplayName("7 - Should return true and collection then try find reservation by place_Id")
    void shouldReturnTrueAndReservationCollection_findReservationByPlaceIdTest() {
        Long testPlaceId = 5L;
        Optional<List<Reservation>> mayBeReservationList =
                reservationRepository.findReservationByPlaceId(testPlaceId);

        assertThat(mayBeReservationList.isPresent()).isTrue();
        assertThat(mayBeReservationList.get().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("8 - Should return zero size collection then try find reservation by not exist place_Id")
    void shouldReturnZeroSizeCollection_findReservationByPlaceIdTest() {
        Long nonExistPlaceId = 15L;
        Optional<List<Reservation>> mayBeReservationList =
                reservationRepository.findReservationByPlaceId(nonExistPlaceId);

        assertThat(mayBeReservationList.get().size()).isEqualTo(0);
    }

    /* Тесты метода *.findReservationBySlotId() */

    @Test
    @DisplayName("9 - Should return true and collection then try find reservation by exist slot_Id")
    void findReservationBySlotId() {
        Long testSlotId = 6L;
        Optional<List<Reservation>> mayBeReservationList =
                reservationRepository.findReservationBySlotId(testSlotId);

        assertThat(mayBeReservationList.isPresent()).isTrue();
        assertThat(mayBeReservationList.get().size()).isEqualTo(5);
    }

    @Test
    @DisplayName("10 - Should return zero size collection then try find reservation by not exist slot_Id")
    void shouldReturnZeroSizeCollection_findReservationBySlotIdTest() {
        Long nonExistSlotId = 15L;
        Optional<List<Reservation>> mayBeReservationList =
                reservationRepository.findReservationBySlotId(nonExistSlotId);

        assertThat(mayBeReservationList.get().size()).isEqualTo(0);
    }

    /* Тесты метода *.findReservationByDatePlaceAndSlot() */

    @Test
    @DisplayName("11 - Should return true / optional reservation if exist combination date-place-slot")
    void shouldReturnTrue_findReservationByDatePlaceAndSlotTest() {
        String testDate = "2029-07-28";
        Long testPlaceId = 4L;
        Long testSlotId = 3L;
        Optional<Reservation> mayBeReservation =
                reservationRepository.findReservationByDatePlaceAndSlot(LocalDate.parse(testDate),
                                                                        testPlaceId,
                                                                        testSlotId);

        assertThat(mayBeReservation.isPresent()).isTrue();
        assertThat(mayBeReservation.get().getReserveId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("12 - Should return false / optional null if non exist combination date-place-slot")
    void shouldReturnFalseAndOptionalNull_findReservationByDatePlaceAndSlotTest() {
        String testDate = "1024-07-28";
        Long testPlaceId = 1L;
        Long testSlotId = 31L;
        Optional<Reservation> mayBeReservation =
                reservationRepository.findReservationByDatePlaceAndSlot(LocalDate.parse(testDate),
                                                                        testPlaceId,
                                                                        testSlotId);

        assertThat(mayBeReservation.isPresent()).isFalse();
    }

    /* Тесты метода *.findReservationByUserId() */

    @Test
    @DisplayName("13 - Should return true / optional reservation list")
    void shouldReturnReservationList_findReservationByUserIdTest() {
        Long testUserId = 1L;
        Optional<List<Reservation>> mayBeReservationList =
                reservationRepository.findReservationByUserId(testUserId);

        assertThat(mayBeReservationList.isPresent()).isTrue();
        assertThat(mayBeReservationList.get().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("14 - Should return empty collection if try find reservation by non existent user")
    void shouldReturnEmptyList_findReservationByUserIdTest() {
        Long testUserId = 15L;
        Optional<List<Reservation>> mayBeReservationList =
                reservationRepository.findReservationByUserId(testUserId);

        assertThat(mayBeReservationList.get().size()).isEqualTo(0);
    }

    /* Тесты метода *.updateReservation() */

    @Test
    @DisplayName("15 - Should return true if update reservation is success")
    void shouldReturnTrueIfReservationUpdateSuccess_updateReservationTest() {
        Long testReservationForUpdate = 8L;
        String dateForUpdate = "2025-05-12";
        Long userIdForUpdate = 1L;
        Long placeIdForUpdate = 5L;
        Long slotIdForUpdate = 7L;

        Reservation forUpdateReservation =
                reservationRepository.findReservationById(testReservationForUpdate).get();

        forUpdateReservation.setReserveDate(LocalDate.parse(dateForUpdate));
        forUpdateReservation.setUser(userRepository.findUserById(userIdForUpdate).get());
        forUpdateReservation.setPlace(placeRepository.findPlaceById(placeIdForUpdate).get());
        forUpdateReservation.setSlot(slotRepository.findSlotById(slotIdForUpdate).get());

        Boolean isUpdateGood = reservationRepository.updateReservation(forUpdateReservation);

        assertThat(isUpdateGood).isTrue();

        assertAll(
                () -> assertThat(LocalDate.parse(dateForUpdate))
                        .isEqualTo(reservationRepository.findReservationById(testReservationForUpdate)
                                                        .get()
                                                        .getReserveDate()),
                () -> assertThat(userIdForUpdate)
                        .isEqualTo(reservationRepository.findReservationById(testReservationForUpdate)
                                                        .get()
                                                        .getUser()
                                                        .getUserId()),
                () -> assertThat(placeIdForUpdate)
                        .isEqualTo(reservationRepository.findReservationById(testReservationForUpdate)
                                                        .get()
                                                        .getPlace()
                                                        .getPlaceId()),
                () -> assertThat(slotIdForUpdate)
                        .isEqualTo(reservationRepository.findReservationById(testReservationForUpdate)
                                                        .get()
                                                        .getSlot()
                                                        .getSlotId())
        );
    }

    @Test
    @DisplayName("16 - Should return false if update reservation is not existed")
    void shouldReturnFalseIfUpdateFail_updateReservationTest() {
        Reservation reservationForUpdate = new Reservation(17L,
                                                            LocalDate.parse("1995-05-05"),
                                                            userRepository.findUserById(1L).get(),
                                                            placeRepository.findPlaceById(1L).get(),
                                                            slotRepository.findSlotById(1L).get());

        Boolean isUpdateGood = reservationRepository.updateReservation(reservationForUpdate);

        assertThat(isUpdateGood).isFalse();
    }

    /* Тесты метода *.deleteReservation() */

    @Test
    @DisplayName("17 - Should return true if deleted reservation is existed")
    void shouldReturnTrueIfDeleteReservationSuccess_deleteReservationTest() {
        Integer sizeOfListBeforeDeletePlace = reservationRepository.findAllReservation().get().size();
        boolean isDeleteGood = reservationRepository.deleteReservation(3L);
        Integer sizeOfListAfterDeletePlace = reservationRepository.findAllReservation().get().size();

        assertThat(isDeleteGood).isTrue();
        assertThat(sizeOfListBeforeDeletePlace).isGreaterThan(sizeOfListAfterDeletePlace);
    }

    @Test
    @DisplayName("18 - Should return false if try delete non existent reservation")
    void shouldReturnFalseIfDeleteReservationFail_deleteReservationTest() {
        Integer sizeOfListBeforeDeletePlace = reservationRepository.findAllReservation().get().size();
        boolean isDeleteGood = reservationRepository.deleteReservation(43L);
        Integer sizeOfListAfterDeletePlace = reservationRepository.findAllReservation().get().size();

        assertThat(isDeleteGood).isFalse();
        assertThat(sizeOfListBeforeDeletePlace).isEqualTo(sizeOfListAfterDeletePlace);
    }
}