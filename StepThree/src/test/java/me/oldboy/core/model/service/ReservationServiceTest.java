package me.oldboy.core.model.service;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.dto.reservations.ReservationCreateDto;
import me.oldboy.core.dto.reservations.ReservationReadDto;
import me.oldboy.core.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.Reservation;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.database.repository.PlaceRepository;
import me.oldboy.core.model.database.repository.ReservationRepository;
import me.oldboy.core.model.database.repository.SlotRepository;
import me.oldboy.core.model.database.repository.UserRepository;
import me.oldboy.exception.ReservationServiceException;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.oldboy.core.model.database.entity.options.Role.ADMIN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private SlotRepository slotRepository;
    private PlaceRepository placeRepository;
    private UserRepository userRepository;
    private ReservationService reservationService;
    private static SessionFactory sessionFactory;
    private static Connection connection;
    private static LiquibaseManager liquibaseManager;
    private ReservationCreateDto nonExistentReservationCreateDto;
    private ReservationUpdateDeleteDto reservationForUpdateAndDeleteDto;
    private Reservation existReservation;
    private Long nonExistentEntityId = 550L;

    /*
    Начинаем тесты.

    Повторим материал:
    - создаем тестовый контейнер;
    - передаем в него параметры доступа к тестовой БД;
    */
    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    /*
    Перед запуском всего пакета тестов:
    - запускаем тестовый контейнер;
    - получаем экземпляр менеджера миграций;
    - запускаем миграцию в тестовом контейнере;
    - получаем доступ к фабрике сессий для связи с тестовой БД;
    - запускаем миграцию, чтобы провалидировать БД средствами Hibernate см. hibernate.cfg.xml
    */
    @BeforeAll
    public static void startTestContainer(){
        liquibaseManager = LiquibaseManager.getInstance();
        postgresContainer.start();

        connection = TestsConnectionManager.getTestBaseConnection(postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword());

        liquibaseManager.migrationsStart(connection);
        sessionFactory = TestsHibernateUtil.buildSessionFactory(postgresContainer);
    }

    /*
    Перед каждым тестом мы:
    - запускаем миграцию, чтобы прогрузить в тестовую БД все (таблицы и) данные для тестов;
    - получаем сессию для работы с БД;
    - получаем экземпляры классов сервиса и репозитория для работы с таблицей places БД;
    - открываем транзакцию;
    */
    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        liquibaseManager.migrationsStart(connection);

        reservationRepository = new ReservationRepository(sessionFactory);
        slotRepository = new SlotRepository(sessionFactory);
        placeRepository = new PlaceRepository(sessionFactory);
        userRepository = new UserRepository(sessionFactory);

        reservationService = new ReservationService(reservationRepository,
                                                    slotRepository,
                                                    placeRepository,
                                                    userRepository);

        reservationRepository.getEntityManager().getTransaction().begin();

        nonExistentReservationCreateDto =
                new ReservationCreateDto(LocalDate.parse("2029-06-12"), 1L, 6L, 6L);

        /* Для теста меняем все кроме ID записи в базе */
        reservationForUpdateAndDeleteDto = ReservationUpdateDeleteDto.builder()
                .reservationId(1L)
                .reservationDate(LocalDate.of(2039,02,28))
                .userId(4L)
                .placeId(6L)
                .slotId(5L)
                .build();

        existReservation = Reservation.builder()
                .reservationId(1L)
                .reservationDate(LocalDate.of(2029,7,28))
                .user(User.builder()
                        .userId(1L)
                        .userName("Admin")
                        .role(ADMIN)
                        .build())
                .place(Place.builder()
                        .placeId(1L)
                        .placeNumber(1)
                        .species(Species.HALL)
                        .build())
                .slot(Slot.builder()
                        .slotId(1L)
                        .slotNumber(10)
                        .timeStart(LocalTime.of(10,00))
                        .timeFinish(LocalTime.of(11,00))
                        .build())
                .build();
    }

    /*
    После каждого теста мы:
    - коммитим транзакцию (вносим изменения в тестовую БД);
    - откатываем тестовую БД в состояние чистые таблицы (с сохранением таковых);

    Метод отката .rollbackDB() имеет параметр глубины отката, он определяется исходя из структуры и содержания
    'changelog' файлов, при текущей реализации проекта у нас было произведено 6-накатов (схема и 5-и таблицы)
    + 10-накатов (заполнение БД и установка SETVAL для каждой таблицы). Экономим время и ресурсы - откатываем
    только заполнение данных. Накаты таблиц и данных идут последовательно от файла с меньшим индексом к файлу
    с большим (предполагается, что разработчик человек последовательный, и соблюдает некий порядок при загрузке
    миграционных скриптов в db.changelog-master.yaml), соответственно откаты идут в обратном порядка, как в
    стеке - LIFO - посчитать не сложно.
    */
    @AfterEach
    public void resetTestBase(){
        reservationRepository.getEntityManager().getTransaction().commit();
        liquibaseManager.rollbackDB(connection, 10);
    }

    /*
    После окончания всего пакета тестов:
    - закрываем фабрику сессий;
    - останавливаем тестовый контейнер;

    Тесты закончены.
    */
    @AfterAll
    public static void stopTestContainer(){
        sessionFactory.close();
        postgresContainer.stop();
    }

    /* Блок тестов - выделим каждую группу тестов, если это необходимо, в отдельный вложенный класс, для удобства */

    /* Тестируем *.create() */

    @Test
    @DisplayName("1 - ReservationService class *.create method test")
    void shouldReturnNewReservationId_createReservationTest() {
        Long resId = reservationService.create(nonExistentReservationCreateDto);
        Optional<ReservationReadDto> mayBeReservation = reservationService.findById(resId);

        assertThat(resId).isNotZero();
        assertThat(resId > 8).isTrue();
        assertAll(
                () -> assertThat(mayBeReservation.get().reservationDate()).isEqualTo(nonExistentReservationCreateDto.getReservationDate()),
                () -> assertThat(mayBeReservation.get().slotId()).isEqualTo(nonExistentReservationCreateDto.getSlotId()),
                () -> assertThat(mayBeReservation.get().placeId()).isEqualTo(nonExistentReservationCreateDto.getPlaceId()),
                () -> assertThat(mayBeReservation.get().userId()).isEqualTo(nonExistentReservationCreateDto.getUserId())
        );
    }

    /* Тестируем *.findById() */

    @Nested
    @DisplayName("2 - ReservationService class *.findById method tests")
    class FindByIdMethodTests {

        @Test
        void shouldReturnFindReservation_findByIdTest() {
            Optional<ReservationReadDto> mayBeReservation =
                    reservationService.findById(existReservation.getReservationId());
            assertThat(mayBeReservation).isPresent();

            assertAll(
                    () -> assertThat(mayBeReservation.get().reservationId()).isEqualTo(existReservation.getReservationId()),
                    () -> assertThat(mayBeReservation.get().reservationDate()).isEqualTo(existReservation.getReservationDate()),
                    () -> assertThat(mayBeReservation.get().slotId()).isEqualTo(existReservation.getSlot().getSlotId()),
                    () -> assertThat(mayBeReservation.get().placeId()).isEqualTo(existReservation.getPlace().getPlaceId()),
                    () -> assertThat(mayBeReservation.get().userId()).isEqualTo(existReservation.getUser().getUserId())
            );
        }

        @Test
        void shouldReturnEmptyOrFalse_findByIdTest() {
            Optional<ReservationReadDto> mayBeReservation =
                    reservationService.findById(nonExistentEntityId);
            assertThat(mayBeReservation).isEmpty();
        }
    }

    /* Тестируем *.findAll() */

    @Test
    @DisplayName("3 - ReservationService class *.findAll method test")
    void shouldReturnSizeOfReservationList_findAllTest() {
        List<ReservationReadDto> mayBeList = null;
        try {
            mayBeList = reservationService.findAll();
        } catch (ReservationServiceException e) {
            throw new RuntimeException(e);
        }
        assertThat(mayBeList.size()).isEqualTo(8);
    }

    /* Тестируем *.findByDatePlaceAndSlot() */

    @Nested
    @DisplayName("4 - ReservationService class *.findByDatePlaceAndSlot method tests")
    class FindByDatePlaceAndSlotMethodTests {

        @Test
        void shouldReturnReservation_findByDatePlaceAndSlotTest() {
            Optional<ReservationReadDto> mayBeReservation =
                    reservationService.findByDatePlaceAndSlot(existReservation.getReservationDate(),
                            existReservation.getPlace().getPlaceId(),
                            existReservation.getSlot().getSlotId());
            assertThat(mayBeReservation).isPresent();

            assertAll(
                    () -> assertThat(mayBeReservation.get().reservationDate()).isEqualTo(existReservation.getReservationDate()),
                    () -> assertThat(mayBeReservation.get().slotId()).isEqualTo(existReservation.getSlot().getSlotId()),
                    () -> assertThat(mayBeReservation.get().placeId()).isEqualTo(existReservation.getPlace().getPlaceId())
            );
        }

        @Test
        void shouldReturnNullOrFalse_findByDatePlaceAndSlotTest() {
            Optional<ReservationReadDto> mayBeReservation =
                    reservationService.findByDatePlaceAndSlot(LocalDate.of(2034, 2, 12), 3L, 3L);
            assertThat(mayBeReservation).isEmpty();
        }
    }

    /* Тестируем *.findByUserId() */

    @Nested
    @DisplayName("5 - ReservationService class *.findByUserId method tests")
    class FindByUserIdMethodTests {

        @Test
        void shouldReturnListReservationOfSingleUser_findByUserIdTest() throws ReservationServiceException {
            Optional<List<ReservationReadDto>> mayBeListReservation =
                    reservationService.findByUserId(existReservation.getUser().getUserId());
            assertThat(mayBeListReservation).isPresent();
            assertThat(mayBeListReservation.get().size()).isEqualTo(3);
        }

        @Test
        void shouldThrowException_findByUserIdTest() {
            assertThatThrownBy(() -> reservationService.findByUserId(nonExistentEntityId))
                    .isInstanceOf(ReservationServiceException.class)
                    .hasMessageContaining("There are no reservations for user with ID - " + nonExistentEntityId + "! " +
                            "Бронирования для пользователя с ID - " + nonExistentEntityId + " отсутствуют!");
        }
    }

    /* Тестируем *.findBySlotId() */

    @Nested
    @DisplayName("6 - ReservationService class *.findBySlotId method tests")
    class FindBySlotIdMethodTests {

        @Test
        void shouldReturnReservationList_findBySlotIdTest() throws ReservationServiceException {
            Optional<List<ReservationReadDto>> mayBeListReservation =
                    reservationService.findBySlotId(existReservation.getSlot().getSlotId());
            assertThat(mayBeListReservation).isPresent();
            assertThat(mayBeListReservation.get().size()).isEqualTo(2);
        }

        @Test
        void shouldThrowException_findBySlotIdTest() {
            assertThatThrownBy(() -> reservationService.findBySlotId(nonExistentEntityId))
                    .isInstanceOf(ReservationServiceException.class)
                    .hasMessageContaining("There are no reservations for " + nonExistentEntityId + "! " +
                            "Бронирования для " + nonExistentEntityId + " отсутствуют!");
        }
    }

    /* Тестируем *.findByPlaceId() */

    @Nested
    @DisplayName("7 - ReservationService class *.findByPlaceId method tests")
    class FindByPlaceIdMethodTests {

        @Test
        void shouldReturnReservationList_findByPlaceIdTest() throws ReservationServiceException {
            Optional<List<ReservationReadDto>> mayBeListReservation =
                    reservationService.findByPlaceId(existReservation.getPlace().getPlaceId());
            assertThat(mayBeListReservation).isPresent();
            assertThat(mayBeListReservation.get().size()).isEqualTo(3);
        }

        @Test
        void shouldThrowException_findByPlaceIdTest() {
            assertThatThrownBy(() -> reservationService.findByPlaceId(nonExistentEntityId))
                    .isInstanceOf(ReservationServiceException.class)
                    .hasMessageContaining("There are no reservations for " + nonExistentEntityId + "! " +
                            "Бронирования для " + nonExistentEntityId + " отсутствуют!");
        }
    }

    /* Тестируем *.findAllFreeSlotsByDate() */

    @Test
    @DisplayName("8 - ReservationService class *.findAllFreeSlotsByDate method test")
    void shouldReturnSizeOfSelectedCollection_findAllFreeSlotsByDateTest() throws ReservationServiceException {
        Map<Long, List<Long>> freeSlotMap =
                reservationService.findAllFreeSlotsByDate(LocalDate.of(2029, 07, 28));
        int sizeSlotCollection = slotRepository.findAll().size(); // Сколько всего слотов

        int freeSlotCollectionSizeByPlaceId_1 = freeSlotMap.get(1L).size(); // Сколько свободных слотов у placeId = 1
        assertThat(sizeSlotCollection - 2).isEqualTo(freeSlotCollectionSizeByPlaceId_1); // Из БД мы знаем что занято 2-а слота

        int freeSlotCollectionSizeByPlaceId_2 = freeSlotMap.get(2L).size(); // Сколько свободных слотов у placeId = 2
        assertThat(sizeSlotCollection - 1).isEqualTo(freeSlotCollectionSizeByPlaceId_2); // Из БД мы знаем что занят 1-н слот

        int freeSlotCollectionSizeByPlaceId_3 = freeSlotMap.get(3L).size(); // Сколько свободных слотов у placeId = 3
        assertThat(sizeSlotCollection).isEqualTo(freeSlotCollectionSizeByPlaceId_3); // Из БД мы знаем что все слоты свободны

        int freeSlotCollectionSizeByPlaceId_4 = freeSlotMap.get(4L).size(); // Сколько свободных слотов у placeId = 4
        assertThat(sizeSlotCollection).isEqualTo(freeSlotCollectionSizeByPlaceId_4); // Из БД мы знаем что все слоты свободны

        int freeSlotCollectionSizeByPlaceId_5 = freeSlotMap.get(5L).size(); // Сколько свободных слотов у placeId = 5
        assertThat(sizeSlotCollection - 1).isEqualTo(freeSlotCollectionSizeByPlaceId_5); // Из БД мы знаем что занят 1-н слот

        int freeSlotCollectionSizeByPlaceId_6 = freeSlotMap.get(6L).size(); // Сколько свободных слотов у placeId = 6
        assertThat(sizeSlotCollection).isEqualTo(freeSlotCollectionSizeByPlaceId_6); // Из БД мы знаем что все слоты свободны

        int freeSlotCollectionSizeByPlaceId_7 = freeSlotMap.get(7L).size(); // Сколько свободных слотов у placeId = 7
        assertThat(sizeSlotCollection).isEqualTo(freeSlotCollectionSizeByPlaceId_7); // Из БД мы знаем что все слоты свободны

        int freeSlotCollectionSizeByPlaceId_8 = freeSlotMap.get(8L).size(); // Сколько свободных слотов у placeId = 8
        assertThat(sizeSlotCollection).isEqualTo(freeSlotCollectionSizeByPlaceId_8); // Из БД мы знаем что все слоты свободны

        int freeSlotCollectionSizeByPlaceId_9 = freeSlotMap.get(9L).size(); // Сколько свободных слотов у placeId = 9
        assertThat(sizeSlotCollection).isEqualTo(freeSlotCollectionSizeByPlaceId_9); // Из БД мы знаем что все слоты свободны
    }

    /* Тестируем *.update() */

    @Test
    @DisplayName("9 - ReservationService class *.update method test")
    void shouldReturnTrueIfUpdateSuccess_updateMethodTest(){
        Long reservationIdForUpdate = reservationForUpdateAndDeleteDto.reservationId();
        ReservationReadDto forUpdateReservation =
                reservationService.findById(reservationIdForUpdate).get();

        assertThat(reservationService.update(reservationForUpdateAndDeleteDto)).isTrue();

        ReservationReadDto afterUpdateReservation =
                reservationService.findById(reservationIdForUpdate).get();

        assertThat(forUpdateReservation).isNotEqualTo(afterUpdateReservation);
        assertAll(
                () -> assertThat(afterUpdateReservation.reservationDate()).isEqualTo(reservationForUpdateAndDeleteDto.reservationDate()),
                () -> assertThat(afterUpdateReservation.slotId()).isEqualTo(reservationForUpdateAndDeleteDto.slotId()),
                () -> assertThat(afterUpdateReservation.placeId()).isEqualTo(reservationForUpdateAndDeleteDto.placeId()),
                () -> assertThat(afterUpdateReservation.userId()).isEqualTo(reservationForUpdateAndDeleteDto.userId())
        );
    }

    /* Тестируем *.delete() */

    @Test
    @DisplayName("10 - ReservationService class *.delete method test")
    void shouldReturnTrueIfDeleteSuccess_deleteMethodTest(){
        Long reservationIdForDelete = reservationForUpdateAndDeleteDto.reservationId();

        assertThat(reservationService.delete(reservationForUpdateAndDeleteDto)).isTrue();

        Optional<ReservationReadDto> afterDeleteReservation =
                reservationService.findById(reservationIdForDelete);

        assertThat(afterDeleteReservation).isEmpty();
    }
}