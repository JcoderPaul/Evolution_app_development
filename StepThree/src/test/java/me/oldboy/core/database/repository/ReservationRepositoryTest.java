package me.oldboy.core.database.repository;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.Reservation;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.model.database.repository.PlaceRepository;
import me.oldboy.core.model.database.repository.ReservationRepository;
import me.oldboy.core.model.database.repository.SlotRepository;
import me.oldboy.core.model.database.repository.UserRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
class ReservationRepositoryTest {

    private ReservationRepository reservationRepository; // Тестируем методы данного класса
    private UserRepository userRepository;
    private SlotRepository slotRepository;
    private PlaceRepository placeRepository;
    private static SessionFactory sessionFactory;  // Связь Hibernate с БД
    private static Connection connection;  // Связь с БД (тестовой)
    private static LiquibaseManager liquibaseManager; // Подключаем стороннее управление БД
    private Reservation notExistReservation;
    private Reservation existReservation;

    /* Делаем тестовый контейнер */

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    /* Настраиваем состояние контейнера и БД, до и после каждого / всех тестов */

    @BeforeAll
    public static void startTestContainer() {
        liquibaseManager = LiquibaseManager.getInstance();
        postgresContainer.start();
        connection = TestsConnectionManager.getTestBaseConnection(postgresContainer.getJdbcUrl(),
                                                             postgresContainer.getUsername(),
                                                             postgresContainer.getPassword()
        );
        liquibaseManager.migrationsStart(connection);

        sessionFactory = TestsHibernateUtil.buildSessionFactory(postgresContainer);
    }

    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        liquibaseManager.migrationsStart(connection);

        reservationRepository = new ReservationRepository(sessionFactory);
        userRepository = new UserRepository(sessionFactory);
        placeRepository = new PlaceRepository(sessionFactory);
        slotRepository = new SlotRepository(sessionFactory);

        reservationRepository.getEntityManager().getTransaction().begin();

        existReservation = Reservation.builder()
                .reservationId(2L)
                .reservationDate(LocalDate.of(2029, 7, 28))
                .user(userRepository.findById(1L).get())
                .place(placeRepository.findById(2L).get())
                .slot(slotRepository.findById(1L).get())
                .build();

        notExistReservation = Reservation.builder()
                .reservationDate(LocalDate.of(2030, 6, 22))
                .user(userRepository.findById(2L).get())
                .place(placeRepository.findById(5L).get())
                .slot(slotRepository.findById(4L).get())
                .build();
    }

    @AfterEach
    public void resetTestBase(){
        reservationRepository.getEntityManager().getTransaction().commit();
        /*
        rollbackDepth = 10 - очистка таблиц с их сохранением
        rollbackDepth = 16 - полная очистка базы, удаление таблиц
        */
        liquibaseManager.rollbackDB(connection, 10);

    }

    @AfterAll
    public static void stopTestContainer(){
        sessionFactory.close();
        postgresContainer.stop();
    }

    /* Блок тестов стандартных методов RepositoryBase */

    @Test
    @DisplayName("1 - create Reservation - Should return generated Reservation ID")
    void shouldReturnCreatedReservation_createTest() {
        Long generateId = reservationRepository.create(notExistReservation).getReservationId();
        assertThat(generateId).isNotNull();
    }

    @Test
    @DisplayName("2 - findById Reservation - Should return existent Reservation")
    void shouldReturnOptionalReservation_findByIdReservationTest() {
        Long existingReservationId = existReservation.getReservationId();
        Optional<Reservation> mayBeReservation = reservationRepository.findById(existingReservationId);

        assertThat(mayBeReservation).isPresent();
        assertAll(
                () -> assertThat(mayBeReservation.get().getReservationId()).isEqualTo(existingReservationId),
                () -> assertThat(mayBeReservation.get().getReservationDate()).isEqualTo(existReservation.getReservationDate()),
                () -> assertThat(mayBeReservation.get().getPlace()).isEqualTo(existReservation.getPlace()),
                () -> assertThat(mayBeReservation.get().getSlot()).isEqualTo(existReservation.getSlot()),
                () -> assertThat(mayBeReservation.get().getUser()).isEqualTo(existReservation.getUser())
        );
    }

    @Test
    @DisplayName("3 - findById Reservation - Should return empty Optional")
    void shouldReturnOptionalEmpty_findByIdReservationTest() {
        Long nonExistentReservationId = 30L;
        Optional<Reservation> mayBeReservation = reservationRepository.findById(nonExistentReservationId);

        assertThat(mayBeReservation).isEmpty();
    }

    @Test
    @DisplayName("4 - update Reservation - Should return update Reservation")
    void shouldReturnTrue_updateExistReservationTest() {
        Long ReservationId = existReservation.getReservationId();
        LocalDate editReservationDate = LocalDate.of(2041, 4, 14);
        existReservation.setReservationDate(editReservationDate);
        User editUserInReservation = userRepository.findById(3L).get();
        existReservation.setUser(editUserInReservation);
        Slot editSlotInReservation = slotRepository.findById(4L).get();
        existReservation.setSlot(editSlotInReservation);
        Place editPlaceInReservation = placeRepository.findById(2L).get();
        existReservation.setPlace(editPlaceInReservation);

        reservationRepository.update(existReservation);
        Reservation isUpdateReservation = reservationRepository.findById(ReservationId).get();

        assertAll(
                () -> assertThat(isUpdateReservation.getReservationDate())
                        .isEqualTo(existReservation.getReservationDate()),
                () -> assertThat(isUpdateReservation.getSlot()).isEqualTo(existReservation.getSlot()),
                () -> assertThat(isUpdateReservation.getUser()).isEqualTo(existReservation.getUser()),
                () -> assertThat(isUpdateReservation.getPlace()).isEqualTo(existReservation.getPlace())
        );
    }

    @Test
    @DisplayName("5 - delete Reservation - Should return true if Reservation is deleted")
    void shouldReturnTrue_deleteReservationTest() {
        Long existReservationId = existReservation.getReservationId();
        reservationRepository.delete(existReservationId);

        assertThat(reservationRepository.findById(existReservationId)).isEmpty();
    }

    @Test
    @DisplayName("6 - findAll Reservations - Should return size of Reservations list")
    void shouldReturnListOfReservations_findAllReservationTest() {
        Integer listSize = reservationRepository.findAll().size();
        assertThat(listSize).isEqualTo(8);
    }

    /* Тестируем findReservationByDate */

    @Test
    @DisplayName("7 - find Reservation By Date - Should return size of Reservations list")
    void shouldReturnListOfReservationsByDate_findAllReservationByDateTest() {
        Optional<List<Reservation>> mayBeList =
                reservationRepository.findReservationByDate(existReservation.getReservationDate());
        assertThat(mayBeList).isPresent();
        assertThat(mayBeList.get().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("8 - find Reservation By Date - Should return 0 size list - have no reservation by concrete date")
    void shouldReturnNullSizeList_findAllReservationByDateTest() {
        Optional<List<Reservation>> mayBeList =
                reservationRepository.findReservationByDate(LocalDate.of(2134,3,4));
        assertThat(mayBeList.get().size()).isEqualTo(0);
    }

    /* Тестируем findReservationByPlaceId */

    @Test
    @DisplayName("9 - find Reservation By PlaceId - Should return size of Reservations list")
    void shouldReturnListOfReservations_findAllReservationByPlaceIdTest() {
        Optional<List<Reservation>> mayBeList =
                reservationRepository.findReservationByPlaceId(existReservation.getPlace().getPlaceId());
        assertThat(mayBeList).isPresent();
        assertThat(mayBeList.get().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("10 - find Reservation By PlaceId - Should return 0 size of list")
    void shouldReturnNullSizeList_findAllReservationByPlaceIdTest() {
        Optional<List<Reservation>> mayBeList =
                reservationRepository.findReservationByPlaceId(50L);
        assertThat(mayBeList.get().size()).isEqualTo(0);
    }

    /* Тестируем findReservationBySlotId */

    @Test
    @DisplayName("11 - find Reservation By SlotId - Should return size of Reservations list")
    void shouldReturnList_findAllReservationBySlotIdTest() {
        Optional<List<Reservation>> mayBeList =
                reservationRepository.findReservationBySlotId(existReservation.getSlot().getSlotId());
        assertThat(mayBeList).isPresent();
        assertThat(mayBeList.get().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("12 - find Reservation By SlotId - Should return 0 size of list")
    void shouldReturnNullSizeList_findAllReservationBySlotIdTest() {
        Optional<List<Reservation>> mayBeList =
                reservationRepository.findReservationBySlotId(50L);
        assertThat(mayBeList.get().size()).isEqualTo(0);
    }

    /*
    Тестируем findAllReservationByCwEntity

    Как я понимаю, так лучше не делать - не надо толкать в один тест все ситуации и исходы
    (одна ситуация - один тест), но это демонстрация и пусть будет комплексный тест, на все
    возможные виды принимаемых методом сущностей.
    */

    @Test
    @DisplayName("13 - find Reservation By - Should return size of Reservations list")
    void shouldReturnListOfReservationsBy_findAllReservationByCwEntityTest() {
        /* Подаем в метод User entity */
        Optional<List<Reservation>> mayBeListByUser =
                reservationRepository.findReservationByCwEntity(existReservation.getUser());
        assertThat(mayBeListByUser).isPresent();
        assertThat(mayBeListByUser.get().size()).isEqualTo(3);

        /* Подаем в метод Slot entity */
        Optional<List<Reservation>> mayBeListBySlot =
                reservationRepository.findReservationByCwEntity(existReservation.getSlot());
        assertThat(mayBeListBySlot).isPresent();
        assertThat(mayBeListBySlot.get().size()).isEqualTo(2);

        /* Подаем в метод Place entity */
        Optional<List<Reservation>> mayBeListByPlace =
                reservationRepository.findReservationByCwEntity(existReservation.getPlace());
        assertThat(mayBeListByPlace).isPresent();
        assertThat(mayBeListByPlace.get().size()).isEqualTo(2);
    }

    /* Тестируем findAllReservationByUserId */

    @Test
    @DisplayName("14 - find Reservation By userId - Should return size of Reservations list")
    void shouldReturnListOfReservationsByUserId_findAllReservationByUserIdTest() {
        Optional<List<Reservation>> mayBeList =
                reservationRepository.findReservationByUserId(existReservation.getUser().getUserId());
        assertThat(mayBeList).isPresent();
        assertThat(mayBeList.get().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("15 - find Reservation By userId - Should return 0 size of list")
    void shouldReturnNullSizeOfReservationsByUserId_findAllReservationByUserIdTest() {
        Optional<List<Reservation>> mayBeList =
                reservationRepository.findReservationByUserId(50L);
        assertThat(mayBeList.get().size()).isEqualTo(0);
    }

    /* Тестируем findReservationByDatePlaceAndSlot */

    @Test
    @DisplayName("16 - find Reservation By Date Place and Slot - Should return Reservations")
    void shouldReturnListOfReservations_findAllReservationByDatePlaceAndSlotTest() {
        Optional<Reservation> mayBeReservation =
                reservationRepository.findReservationByDatePlaceAndSlot(existReservation.getReservationDate(),
                                                                        existReservation.getPlace().getPlaceId(),
                                                                        existReservation.getSlot().getSlotId());
        assertThat(mayBeReservation).isPresent();
    }

    @Test
    @DisplayName("17 - find Reservation By Date Place and Slot - Should return null")
    void shouldReturnNullSizeList_findAllReservationByDatePlaceAndSlotIdTest() {
        Optional<Reservation> mayBeReservation =
                reservationRepository.findReservationByDatePlaceAndSlot(LocalDate.of(2345, 4,5),
                                                                  50L,
                                                                   45L);
        assertThat(mayBeReservation).isEmpty();
    }
}