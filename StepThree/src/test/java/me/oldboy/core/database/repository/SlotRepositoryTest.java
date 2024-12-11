package me.oldboy.core.database.repository;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.repository.SlotRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
class SlotRepositoryTest {

    private SlotRepository slotRepository; // Тестируем методы данного класса
    private static SessionFactory sessionFactory;  // Связь Hibernate с БД
    private static Connection connection;  // Связь с БД (тестовой)
    private static LiquibaseManager liquibaseManager; // Подключаем стороннее управление БД
    private Slot notExistSlot;
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
                                                             postgresContainer.getPassword()
        );
        liquibaseManager.migrationsStart(connection);

        sessionFactory = TestsHibernateUtil.buildSessionFactory(postgresContainer);
    }

    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        liquibaseManager.migrationsStart(connection);

        slotRepository = new SlotRepository(sessionFactory);
        slotRepository.getEntityManager().getTransaction().begin();

        existSlot = Slot.builder()
                .slotId(2L)
                .slotNumber(11)
                .timeStart(LocalTime.of(11,00))
                .timeFinish(LocalTime.of(12,00))
                .build();

        notExistSlot = Slot.builder()
                .slotNumber(19)
                .timeStart(LocalTime.of(19,00))
                .timeFinish(LocalTime.of(20,00))
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

    /* Блок тестов */

    @Test
    @DisplayName("1 - create Slot - Should return generated slot ID")
    void shouldReturnCreatedSlotTest() {
        Long generateId = slotRepository.create(notExistSlot).getSlotId();
        assertThat(generateId).isNotNull();
    }

    @Test
    @DisplayName("2 - findById Slot - Should return existent slot")
    void shouldReturnOptionalSlot_findByIdSlotTest() {
        Long existingSlotId = existSlot.getSlotId();
        Optional<Slot> mayBeSlot = slotRepository.findById(existingSlotId);

        assertThat(mayBeSlot).isPresent();
        assertAll(
                () -> assertThat(mayBeSlot.get().getSlotId()).isEqualTo(existingSlotId),
                () -> assertThat(mayBeSlot.get().getSlotNumber()).isEqualTo(existSlot.getSlotNumber()),
                () -> assertThat(mayBeSlot.get().getTimeStart()).isEqualTo(existSlot.getTimeStart()),
                () -> assertThat(mayBeSlot.get().getTimeFinish()).isEqualTo(existSlot.getTimeFinish())
        );
    }

    @Test
    @DisplayName("3 - findById Slot - Should return existent slot")
    void shouldReturnOptionalEmpty_findByIdIfNotExistSlotTest(){
        Long nonExistentSlotId = 20L;
        Optional<Slot> mayBeSlot = slotRepository.findById(nonExistentSlotId);

        assertThat(mayBeSlot).isEmpty();
    }

    @Test
    @DisplayName("4 - update Slot - Should return update slot")
    void shouldReturnTrue_updateExistSlotTest() {
        Long slotId = existSlot.getSlotId();
        Integer slotNumber = 1030;
        existSlot.setSlotNumber(slotNumber);
        LocalTime newTimeStart= LocalTime.of(10,30);
        existSlot.setTimeStart(newTimeStart);
        LocalTime newTimeFinish= LocalTime.of(10,59);
        existSlot.setTimeFinish(newTimeFinish);

        slotRepository.update(existSlot);
        Slot isUpdateSlot = slotRepository.findById(slotId).get();

        assertAll(
                () -> assertThat(isUpdateSlot.getSlotNumber()).isEqualTo(existSlot.getSlotNumber()),
                () -> assertThat(isUpdateSlot.getTimeStart()).isEqualTo(existSlot.getTimeStart()),
                () -> assertThat(isUpdateSlot.getTimeFinish()).isEqualTo(existSlot.getTimeFinish())
        );
    }

    @Test
    @DisplayName("5 - delete Slot - Should return true if slot is deleted")
    void shouldReturnTrue_deleteSlotTest() {
        Long existSlotId = existSlot.getSlotId();
        slotRepository.delete(existSlotId);

        assertThat(slotRepository.findById(existSlotId)).isEmpty();
    }

    @Test
    @DisplayName("6 - findAll Slots - Should return size of slots list")
    void shouldReturnListOfSlots_findAllSlotTest() {
        Integer listSize = slotRepository.findAll().size();
        assertThat(listSize).isEqualTo(9);
    }

    @Test
    @DisplayName("7 - findSlotByNumber Slots - Should return true if slot exist")
    void shouldReturnTrue_findSlotByNumberSlotTest() {
        Optional<Slot> mayBySlot= slotRepository.findSlotByNumber(existSlot.getSlotNumber());
        assertThat(mayBySlot).isPresent();
    }

    @Test
    @DisplayName("8 - findSlotByNumber - Should return optional empty or false if slot is non existent")
    void shouldReturnFalse_findSlotByNumberSlotTest() {
        Optional<Slot> mayBySlot= slotRepository.findSlotByNumber(40);
        assertThat(mayBySlot).isEmpty();
    }
}