package me.oldboy.core.model.service;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.dto.slots.SlotCreateDeleteDto;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
import me.oldboy.core.model.database.repository.ReservationRepository;
import me.oldboy.exception.SlotServiceException;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.repository.SlotRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.validation.ConstraintViolationException;
import java.sql.Connection;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class SlotServiceTest {

    private SlotRepository slotRepository;
    private ReservationRepository reservationRepository;
    private SlotService slotService;
    private static SessionFactory sessionFactory;  // Связь Hibernate с БД
    private static Connection connection;  // Связь с БД (тестовой)
    private static LiquibaseManager liquibaseManager; // Подключаем стороннее управление БД
    private SlotCreateDeleteDto notExistSlot;
    private SlotReadUpdateDto existSlotDto, updateSlotData;
    private Slot existSlot;

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
                                                             postgresContainer.getPassword());

        liquibaseManager.migrationsStart(connection);

        sessionFactory = TestsHibernateUtil.buildSessionFactory(postgresContainer);
    }

    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        liquibaseManager.migrationsStart(connection);

        slotRepository = new SlotRepository(sessionFactory);
        reservationRepository = new ReservationRepository(sessionFactory);
        slotService = new SlotService(slotRepository, reservationRepository);

        slotRepository.getEntityManager().getTransaction().begin();

        notExistSlot = new SlotCreateDeleteDto(19, LocalTime.parse("19:00"), LocalTime.parse("20:00"));
        updateSlotData = new SlotReadUpdateDto(9L, 1830, LocalTime.parse("18:30"), LocalTime.parse("19:00"));
        existSlotDto = new SlotReadUpdateDto(1L,10, LocalTime.parse("10:00"), LocalTime.parse("11:00"));

        existSlot = Slot.builder()
                .slotId(1L)
                .slotNumber(10)
                .timeStart(LocalTime.parse("10:00"))
                .timeFinish(LocalTime.parse("11:00"))
                .build();
    }

    @AfterEach
    public void resetTestBase(){
        slotRepository.getEntityManager().getTransaction().commit();
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

    /* Блок тестов - выделим каждую группу тестов, если это необходимо, в отдельный вложенный класс, для удобства */

    /* Тестируем метод .findById() */

    @Nested
    @DisplayName("1 - SlotService class *.findById method tests")
    class FindByIdMethodTests {

        @Test
        void shouldReturnSlotReadDto_findSlotById() {
            Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(existSlot.getSlotId());

            assertThat(mayBeSlot).isPresent();

            assertAll(
                    () -> assertThat(mayBeSlot.get().slotId()).isEqualTo(existSlot.getSlotId()),
                    () -> assertThat(mayBeSlot.get().slotNumber()).isEqualTo(existSlot.getSlotNumber()),
                    () -> assertThat(mayBeSlot.get().timeStart()).isEqualTo(existSlot.getTimeStart()),
                    () -> assertThat(mayBeSlot.get().timeFinish()).isEqualTo(existSlot.getTimeFinish())
            );
        }

        @Test
        void shouldReturnFalseOrNullIfSlotNonExistent_findSlotById() {
            Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(20L);
            assertThat(mayBeSlot).isEmpty();
        }
    }

    /* Тестируем метод .create() */

    @Nested
    @DisplayName("2 - SlotService class *.create method tests")
    class CreateMethodTests {

        @Test
        void shouldReturnGeneratedSlotId_createNewSlot() {
            Long generatedId = slotService.create(notExistSlot);

            assertThat(generatedId).isNotZero();

            Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(generatedId);

            assertThat(mayBeSlot).isPresent();
            assertAll(
                    () -> assertThat(mayBeSlot.get().slotId()).isEqualTo(generatedId),
                    () -> assertThat(mayBeSlot.get().slotNumber()).isEqualTo(notExistSlot.slotNumber()),
                    () -> assertThat(mayBeSlot.get().timeStart()).isEqualTo(notExistSlot.timeStart()),
                    () -> assertThat(mayBeSlot.get().timeFinish()).isEqualTo(notExistSlot.timeFinish())
            );
        }

        /* Тестируем метод .create() на Exceptions */

        @Test
        void shouldThrowExceptionTimeConflictRightRange_createNewSlotTest() {
            SlotCreateDeleteDto slotWithTimeRangeConflict =
                    new SlotCreateDeleteDto(1830, LocalTime.parse("18:30"), LocalTime.parse("19:30"));

            assertThatThrownBy(() -> slotService.create(slotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Конфликт временного диапазона слота бронирования!");
        }

        @Test
        void shouldThrowExceptionTimeConflictLeftRange_createNewSlotTest() {
            SlotCreateDeleteDto slotWithTimeRangeConflict =
                    new SlotCreateDeleteDto(930, LocalTime.parse("09:30"), LocalTime.parse("10:30"));
            assertThatThrownBy(() -> slotService.create(slotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Конфликт временного диапазона слота бронирования!");
        }

        @Test
        void shouldThrowExceptionTimeConflictInnerRange_createNewSlotTest() {
            SlotCreateDeleteDto slotWithTimeRangeConflict =
                    new SlotCreateDeleteDto(1030, LocalTime.parse("10:30"), LocalTime.parse("10:45"));
            assertThatThrownBy(() -> slotService.create(slotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Конфликт временного диапазона слота бронирования!");
        }

        @Test
        void shouldThrowExceptionDuplicateSlotNumber_createNewSlotTest() {
            SlotCreateDeleteDto slotWithTimeRangeConflict =
                    new SlotCreateDeleteDto(10, LocalTime.parse("20:30"), LocalTime.parse("21:45"));

            assertThatThrownBy(() -> slotService.create(slotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Слот с номером '" + slotWithTimeRangeConflict.slotNumber() + "' уже существует!");
        }

        @Test
        void shouldThrowExceptionUnacceptableTimeRange_createNewSlotTest() {
            SlotCreateDeleteDto slotWithTimeRangeConflict =
                    new SlotCreateDeleteDto(21, LocalTime.parse("21:30"), LocalTime.parse("21:15"));

            assertThatThrownBy(() -> slotService.create(slotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Время начала: " +
                            slotWithTimeRangeConflict.timeStart() +
                            " не может быть установлено позже времени окончания слота: " +
                            slotWithTimeRangeConflict.timeFinish());
        }

        @Test
        void shouldThrowValidationException_createNewSlotTest() {
            SlotCreateDeleteDto slotWithTimeRangeConflict =
                    new SlotCreateDeleteDto(-21, LocalTime.parse("21:00"), LocalTime.parse("22:00"));

            assertThatThrownBy(() -> slotService.create(slotWithTimeRangeConflict))
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }

    /* Тестируем метод .findAll() */

    @Test
    @DisplayName("3 - SlotService class *.findAll method test")
    void shouldReturnListOfSlotReadDto_findAllSlotTest() {
        List<SlotReadUpdateDto> slotReadUpdateDtoList = slotService.findAll();

        assertThat(slotReadUpdateDtoList.size()).isEqualTo(9);
    }

    /* Тестируем метод .findSlotByNumber */

    @Nested
    @DisplayName("4 - SlotService class *.findSlotByNumber method tests")
    class FindSlotByNumberMethodTests {

        @Test
        void shouldReturnSlotReadDtoIfExist_findSlotByNumberTest() {
            Optional<SlotReadUpdateDto> mayBeSlotReadDto = slotService.findSlotByNumber(existSlot.getSlotNumber());
            assertThat(mayBeSlotReadDto).isPresent();
        }

        @Test
        void shouldReturnFalseIfNonExistent_findSlotByNumberTest() {
            Optional<SlotReadUpdateDto> mayBeSlotReadDto = slotService.findSlotByNumber(67);
            assertThat(mayBeSlotReadDto).isEmpty();
        }
    }

    /* Тестируем метод .delete */

    @Nested
    @DisplayName("5 - SlotService class *.delete method tests")
    class DeleteMethodTests {

        @Test
        void shouldReturnTrue_deleteSlotTest() {
            assertThat(slotService.delete(existSlot.getSlotId())).isTrue();
        }

        @Test
        void shouldReturnFalse_deleteSlotTest() {
            assertThat(slotService.delete(15L)).isFalse();
        }
    }

    /* Тестируем метод .update */

    @Nested
    @DisplayName("6 - SlotService class *.update method tests")
    class UpdateMethodTests {

        @Test
        void shouldReturnTrue_updateSlotTest() {
            assertThat(slotService.update(9L, updateSlotData)).isTrue();
        }

        @Test
        void shouldReturnFalseIfTryToUpdateNonExistentSlot_updateSlotTest() {
            assertThat(slotService.update(19L, updateSlotData)).isFalse();
        }


        /* Тестируем метод .update() на Exceptions */

        @Test
        void shouldThrowValidationException_updateSlotTest() {
            assertThatThrownBy(() ->
                    slotService.update(9L, new SlotReadUpdateDto(9L,
                            -20,
                            LocalTime.parse("18:05"),
                            LocalTime.parse("18:45"))))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("Slot number can not be blank/null/negative, it must be greater than or equal to 0");
        }

        @Test
        void shouldThrowUpdateExceptionDuplicateSlotNumber_updateSlotTest() {
            SlotReadUpdateDto updateSlotWithTimeRangeConflict =
                    new SlotReadUpdateDto(existSlot.getSlotId(), 17, LocalTime.parse("10:10"), LocalTime.parse("10:45"));
            assertThatThrownBy(() -> slotService.update(existSlot.getSlotId(), updateSlotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Слот с номером " + "'" + updateSlotWithTimeRangeConflict.slotNumber() + "' уже существует!");
        }

        @Test
        void shouldThrowExceptionUnacceptableTimeRange_updateSlotTest() {
            SlotReadUpdateDto slotWithTimeRangeConflict =
                    new SlotReadUpdateDto(existSlot.getSlotId(), 10, LocalTime.parse("10:40"), LocalTime.parse("10:15"));
            assertThatThrownBy(() -> slotService.update(existSlot.getSlotId(), slotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Время начала: " + slotWithTimeRangeConflict.timeStart() +
                            " не может быть установлено позже времени окончания слота: " + slotWithTimeRangeConflict.timeFinish());
        }

        @Test
        void shouldThrowExceptionTimeConflictRightRange_updateSlotTest() {
            SlotReadUpdateDto slotWithTimeRangeConflict =
                    new SlotReadUpdateDto(existSlot.getSlotId(), 10, LocalTime.parse("09:30"), LocalTime.parse("10:30"));
            assertThatThrownBy(() -> slotService.update(existSlot.getSlotId(), slotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Обновить временной диапазон можно только в переделах текущего!");
        }

        @Test
        void shouldThrowExceptionTimeConflictLeftRange_updateSlotTest() {
            SlotReadUpdateDto slotWithTimeRangeConflict =
                    new SlotReadUpdateDto(existSlot.getSlotId(), 10, LocalTime.parse("10:00"), LocalTime.parse("11:30"));
            assertThatThrownBy(() -> slotService.update(existSlot.getSlotId(), slotWithTimeRangeConflict))
                    .isInstanceOf(SlotServiceException.class)
                    .hasMessageContaining("Обновить временной диапазон можно только в переделах текущего!");
        }
    }
}