package me.oldboy.cwapp.core.repository;

import me.oldboy.cwapp.config.connection.ConnectionManager;
import me.oldboy.cwapp.config.liquibase.LiquibaseManager;
import me.oldboy.cwapp.core.entity.Slot;
import me.oldboy.cwapp.core.repository.crud.SlotRepository;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/* Аннотация говорит, что тестирование методов класса идет через Docker тест-контейнер */
@Testcontainers
class SlotRepositoryImplTest {

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
        slotRepository = new SlotRepositoryImpl(connection);
    }

    @AfterEach
    public void resetTestBase(){
        liquibaseManager.rollbackCreatedTables(connection);
    }

    @AfterAll
    public static void stopTestContainer(){
        postgresContainer.stop();
    }

    /* Основные тесты для реализаций методов SlotRepository */

    @Test
    @DisplayName("1 - Should return creation optional slot")
    void shouldReturnTrueCreateOptionalSlot_createSlotTest() {
        Slot createSlot =
                new Slot(6, LocalTime.of(6,00), LocalTime.of(7,00));
        Optional<Slot> mayBeCreateSlot = slotRepository.createSlot(createSlot);

        assertThat(mayBeCreateSlot.isPresent()).isTrue();

        assertAll(
                () -> assertThat(createSlot.getSlotNumber())
                        .isEqualTo(mayBeCreateSlot.get().getSlotNumber()),
                () -> assertThat(createSlot.getTimeStart())
                        .isEqualTo(mayBeCreateSlot.get().getTimeStart()),
                () -> assertThat(createSlot.getTimeFinish())
                        .isEqualTo(mayBeCreateSlot.get().getTimeFinish())
        );
    }

    /* Тест метода *.findAllSlots */

    @Test
    @DisplayName("2 - Should return list of slots")
    void shouldReturnListOfSlots_findAllSlotsTest() {
        List<Slot> slotsList = slotRepository.findAllSlots();

        assertThat(slotsList.size()).isEqualTo(9);

        assertAll(
                () -> assertThat(slotsList.get(0).getSlotNumber())
                        .isEqualTo(10),
                () -> assertThat(slotsList.get(slotsList.size() - 1).getSlotNumber())
                        .isEqualTo(18)
        );
    }

    /* Тесты метода *.findUserById */

    @Test
    @DisplayName("3 - Should return optional slot find by ID")
    void shouldReturnOptionalSlotIfExist_findSlotByIdTest() {
        Long testSlotId = 1L;
        Optional<Slot> findSlot = slotRepository.findSlotById(testSlotId);

        assertThat(findSlot.isPresent()).isTrue();

        assertAll(
                () -> assertThat(findSlot.get().getSlotId()).isEqualTo(testSlotId),
                () -> assertThat(findSlot.get().getSlotNumber()).isEqualTo(10),
                () -> assertThat(findSlot.get().getTimeStart()).isEqualTo(LocalTime.parse("10:00")),
                () -> assertThat(findSlot.get().getTimeFinish()).isEqualTo(LocalTime.parse("11:00"))
        );
    }

    @Test
    @DisplayName("4 - Should return Optional null/false to try find non existent slot by ID")
    void shouldReturnOptionalNull_findNonExistentPlaceByIdTest() {
        Optional<Slot> findNonExistentSlot = slotRepository.findSlotById(15L);

        assertThat(findNonExistentSlot.isPresent()).isFalse();
    }

    /* Тесты метода *.findSlotByNumber() */

    @Test
    @DisplayName("5 - Should return optional slot find by number")
    void shouldReturnTrueOptionalSlot_findSlotByNumberTest() {
        Integer testNumberToFind = 11;
        Optional<Slot> mayBeFindSlotByNumber = slotRepository.findSlotByNumber(testNumberToFind);

        assertThat(mayBeFindSlotByNumber.isPresent()).isTrue();

        assertAll(
                () -> mayBeFindSlotByNumber.get().getSlotNumber().equals(testNumberToFind),
                () -> mayBeFindSlotByNumber.get().getSlotId().equals(2),
                () -> mayBeFindSlotByNumber.get().getTimeStart().equals(LocalTime.parse("11:00")),
                () -> mayBeFindSlotByNumber.get().getTimeFinish().equals(LocalTime.parse("12:00"))
        );
    }

    @Test
    @DisplayName("6 - Should return false / optional null if not find slot by number")
    void shouldReturnFalseOptionalNullIfSlotNonExist_findSlotByNumberTest() {
        Integer notExistedSlotNumber = 245;
        Optional<Slot> findNonExistentSlot = slotRepository.findSlotByNumber(notExistedSlotNumber);

        assertThat(findNonExistentSlot.isPresent()).isFalse();
    }

    /* Тесты метода *.updateSlot() */

    @Test
    @DisplayName("7 - Should return true if update existed slot")
    void shouldReturnTrueIfUpdateSuccess_updateSlotTest() {
        Integer testSlotNumber = 10;
        Slot slotForUpdate =
                new Slot(1L, testSlotNumber, LocalTime.parse("10:01"), LocalTime.parse("10:59"));

        Boolean isUpdateGood = slotRepository.updateSlot(slotForUpdate);
        assertThat(isUpdateGood).isTrue();

        assertAll(
                () -> assertThat(slotForUpdate.getSlotId())
                        .isEqualTo(slotRepository.findSlotByNumber(testSlotNumber).get().getSlotId()),
                () -> assertThat(slotForUpdate.getSlotNumber())
                        .isEqualTo(slotRepository.findSlotByNumber(testSlotNumber).get().getSlotNumber()),
                () -> assertThat(slotForUpdate.getTimeStart())
                        .isEqualTo(slotRepository.findSlotByNumber(testSlotNumber).get().getTimeStart()),
                () -> assertThat(slotForUpdate.getTimeFinish())
                        .isEqualTo(slotRepository.findSlotByNumber(testSlotNumber).get().getTimeFinish())
        );
    }

    @Test
    @DisplayName("8 - Should return false if update not existed slot")
    void shouldReturnFalseIfUpdateFail_updateSlotTest() {
        Slot slotForUpdate =
                new Slot(15L, 25, LocalTime.parse("10:01"), LocalTime.parse("10:59"));
        Boolean isUpdateGood = slotRepository.updateSlot(slotForUpdate);

        assertThat(isUpdateGood).isFalse();
    }

    /* Тесты метода *.deleteSlot() */

    @Test
    @DisplayName("9 - Should return true if slot is deleted success")
    void shouldReturnTrueIfDeleteSuccess_deleteSlotTest() {
        Integer sizeOfListBeforeDeletePlace = slotRepository.findAllSlots().size();
        boolean isDeleteGood = slotRepository.deleteSlot(9L);
        Integer sizeOfListAfterDeletePlace = slotRepository.findAllSlots().size();

        assertThat(isDeleteGood).isTrue();
        assertThat(sizeOfListBeforeDeletePlace).isGreaterThan(sizeOfListAfterDeletePlace);
    }

    @Test
    @DisplayName("10 - Should return false if slot is not deleted/non existent")
    void shouldReturnFalseIfDeleteFail_deleteSlotTest() {
        Integer sizeOfListBeforeDeletePlace = slotRepository.findAllSlots().size();
        boolean isDeleteGood = slotRepository.deleteSlot(32L);
        Integer sizeOfListAfterDeletePlace = slotRepository.findAllSlots().size();

        assertThat(isDeleteGood).isFalse();
        assertThat(sizeOfListBeforeDeletePlace).isEqualTo(sizeOfListAfterDeletePlace);
    }
}