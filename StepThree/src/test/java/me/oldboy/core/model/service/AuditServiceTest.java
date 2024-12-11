package me.oldboy.core.model.service;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.dto.slots.SlotCreateDeleteDto;
import me.oldboy.core.model.database.audit.Audit;
import me.oldboy.core.model.database.audit.operations.AuditOperationResult;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.core.model.database.repository.AuditRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {

    private AuditService auditService;
    private AuditRepository auditRepository;
    private static SessionFactory sessionFactory;
    private static Connection connection;
    private static LiquibaseManager liquibaseManager;
    private Audit notExistAuditRecord;

    /* Шаг 1 - Создаем тестовый контейнер */

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    /* Шаг 2 - Стартуем тестовый контейнер, соединяемся с БД и стартуем миграцию */

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

    /* Шаг 3 - Инициализируем основные рабочие объекты и открываем транзакцию */

    @BeforeEach
    public void getConnectionToTestBase(){
        liquibaseManager.migrationsStart(connection);

        auditRepository = new AuditRepository(sessionFactory);
        auditService = AuditService.getInstance(auditRepository);

        auditRepository.getEntityManager().getTransaction().begin();

        notExistAuditRecord = Audit.builder()
                .userName("tester")
                .auditResult(AuditOperationResult.SUCCESS)
                .operationType(AuditOperationType.CREATE_SLOT)
                .auditableRecord(String.valueOf(new SlotCreateDeleteDto(19, LocalTime.parse("19:00"), LocalTime.parse("20:00"))))
                .auditTimeStamp(LocalDateTime.now())
                .build();
    }

    /* Шаг 4 - Коммитим транзакцию и откатываем состояние БД до чистых таблиц */

    @AfterEach
    public void resetTestBase(){
        auditRepository.getEntityManager().getTransaction().commit();
        /*
        rollbackDepth = 10 - очистка таблиц с их сохранением
        rollbackDepth = 16 - полная очистка базы, удаление таблиц
        */
        liquibaseManager.rollbackDB(connection, 10);
    }

    /* Шаг 5 - Закрываем сессионную фабрику и останавливаем тестовый контейнер */

    @AfterAll
    public static void stopTestContainer(){
        sessionFactory.close();
        postgresContainer.stop();
    }

    /* Блок основных тестов */

    @Test
    void getAllAudit() {
        int auditRecordListSize = auditService.getAllAudit().size();
        assertTrue(auditRecordListSize == 3);
    }

    @Test
    void saveAudRecordTest() {
        auditService.saveAudRecord(notExistAuditRecord.getUserName(),
                                   notExistAuditRecord.getOperationType(),
                                   notExistAuditRecord.getAuditableRecord(),
                                   notExistAuditRecord.getAuditResult());

        int auditRecordListSize = auditService.getAllAudit().size();
        assertTrue(auditRecordListSize == 4);
    }
}