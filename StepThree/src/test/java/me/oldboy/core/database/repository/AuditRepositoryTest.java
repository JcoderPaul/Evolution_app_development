package me.oldboy.core.database.repository;

/*
По факту, из всего комплекса методов предоставленных абстрактным классом RepositoryBase
нам нужен в работе лишь *.create() и возможно *.findAll(), но мы протестируем все.
*/

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.model.database.audit.Audit;
import me.oldboy.core.model.database.audit.operations.AuditOperationResult;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.database.repository.AuditRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
class AuditRepositoryTest {

    private AuditRepository auditRepository; // Тестируем методы данного класса
    private static SessionFactory sessionFactory;  // Связь Hibernate с БД
    private static Connection connection;  // Связь с БД (тестовой)
    private static LiquibaseManager liquibaseManager; // Подключаем стороннее управление БД
    private Audit notExistAudit;
    private Audit existAudit;

    /* ШАГ 1 - Делаем тестовый контейнер */

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    /* ШАГ 2 (Перед запуском всех тестов) - Настраиваем состояние контейнера и БД, до и после каждого / всех тестов */

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

    /* ШАГ 3 (Перед каждым тестом) - Инициализируем класс для связи с БД и открываем транзакцию */

    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        liquibaseManager.migrationsStart(connection);

        auditRepository = new AuditRepository(sessionFactory);
        auditRepository.getEntityManager().getTransaction().begin();

        /* Инициализируем рабочие копии сущностей */
        notExistAudit = Audit.builder()
                .userName("UserUser")
                .auditTimeStamp(LocalDateTime.now().plusYears(1))
                .auditResult(AuditOperationResult.SUCCESS)
                .operationType(AuditOperationType.CREATE_PLACE)
                .auditableRecord(String.valueOf(new PlaceCreateDeleteDto(Species.WORKPLACE, 8))) // В базу мы заносим не сами (созданные, удаленные, измененные) сущности, а их строковые представления
                .build();

        existAudit = Audit.builder()
                .auditId(1L)
                .userName("nameOf")
                .auditResult(AuditOperationResult.SUCCESS)
                .auditTimeStamp(LocalDateTime.parse("2024-11-17T16:28:29"))
                .operationType(AuditOperationType.CREATE_RESERVATION)
                .auditableRecord("ReservationCreate {reservationDate: 2033-08-11, userId = 3, placeId = 8, slotId = 1}") // В базу мы заносим не сами (созданные, удаленные, измененные) сущности, а их строковые представления
                .build();
    }

    /* ШАГ 4 (После каждого теста) - Коммитим транзакцию и откатываем БД до чистых таблиц */

    @AfterEach
    public void resetTestBase(){
        auditRepository.getEntityManager().getTransaction().commit();
        /*
        rollbackDepth = 10 - очистка таблиц с их сохранением
        rollbackDepth = 16 - полная очистка базы, удаление таблиц
        */
        liquibaseManager.rollbackDB(connection, 10);

    }

    /* ШАГ 5 (После завершения всех тестов) - Закрываем фабрику сессий и стопорим тест-контейнер */

    @AfterAll
    public static void stopTestContainer(){
        sessionFactory.close();
        postgresContainer.stop();
    }

    /* Начинаем тесты */

    @Test
    @DisplayName("1 - create AuditRecord - Should return generated audit ID")
    void shouldReturnCreatedAuditRecordIdTest() {
        Long generateId = auditRepository.create(notExistAudit).getAuditId();
        assertThat(generateId).isNotNull();
    }

    @Test
    @DisplayName("2 - findById Audit - Should return existent audit record")
    void shouldReturnOptionalAuditRecord_findByIdAuditRecordTest() {
        Long existingAuditId = existAudit.getAuditId();
        Optional<Audit> mayBeAudit = auditRepository.findById(existingAuditId);

        assertThat(mayBeAudit).isPresent();
        assertAll(
                () -> assertThat(mayBeAudit.get().getAuditId()).isEqualTo(existingAuditId),
                () -> assertThat(mayBeAudit.get().getUserName()).isEqualTo(existAudit.getUserName()),
                () -> assertThat(mayBeAudit.get().getAuditableRecord()).isEqualTo(existAudit.getAuditableRecord()),
                () -> assertThat(mayBeAudit.get().getAuditResult()).isEqualTo(existAudit.getAuditResult())
        );
    }

    @Test
    @DisplayName("3 - findById Audit - Should return empty Optional")
    void shouldReturnOptionalEmpty_findByIdAuditRecordTest() {
        Long nonExistentAuditId = 20L;
        Optional<Audit> mayBeAudit = auditRepository.findById(nonExistentAuditId);

        assertThat(mayBeAudit).isEmpty();
    }

    /*
    Данного метода в принципе быть не должно для секции аудита, т.к.
    каждый встречный поперечный шлемиль будет править свои косяки
    */

    @Test
    @DisplayName("4 - update Audit - Should return update audit")
    void shouldReturnTrue_updateExistAuditRecordTest() {
        Long auditId = 1L;
        String newName = "notMe";
        existAudit.setUserName(newName);
        AuditOperationResult newResult = AuditOperationResult.FAIL;
        existAudit.setAuditResult(newResult);
        AuditOperationType newOperation = AuditOperationType.CREATE_PLACE;
        existAudit.setOperationType(newOperation);
        String auditEntity = "PlaceCreateDeleteDto { species = HALL, placeNumber = 65}";
        existAudit.setAuditableRecord(auditEntity);
        LocalDateTime newTime = LocalDateTime.now().plusYears(1);
        existAudit.setAuditTimeStamp(newTime);

        auditRepository.update(existAudit);
        Audit isUpdateAudit = auditRepository.findById(auditId).get();

        assertAll(
                () -> assertThat(isUpdateAudit.getUserName()).isEqualTo(existAudit.getUserName()),
                () -> assertThat(isUpdateAudit.getAuditableRecord()).isEqualTo(existAudit.getAuditableRecord()),
                () -> assertThat(isUpdateAudit.getAuditResult()).isEqualTo(existAudit.getAuditResult()),
                () -> assertThat(isUpdateAudit.getOperationType()).isEqualTo(existAudit.getOperationType()),
                () -> assertThat(isUpdateAudit.getAuditTimeStamp()).isEqualTo(existAudit.getAuditTimeStamp())
        );
    }

    @Test
    @DisplayName("5 - delete Audit - Should return true if audit record is deleted or optional empty")
    void shouldReturnTrue_deleteAuditRecordTest() {
        Long existAuditId = existAudit.getAuditId();
        auditRepository.delete(existAuditId);

        assertThat(auditRepository.findById(existAuditId)).isEmpty();
    }

    @Test
    @DisplayName("6 - findAll Audits - Should return size of audit list")
    void shouldReturnListSize_findAllAuditRecordTest() {
        Integer listSize = auditRepository.findAll().size();
        assertThat(listSize).isEqualTo(3);
    }
}