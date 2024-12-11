package me.oldboy.core.database.repository;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.model.database.repository.UserRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
class UserRepositoryTest {

    private UserRepository userRepository; // Тестируем методы данного класса
    private static SessionFactory sessionFactory;  // Связь Hibernate с БД
    private static Connection connection;  // Связь с БД (тестовой)
    private static LiquibaseManager liquibaseManager; // Подключаем стороннее управление БД
    private User notExistUser;
    private User existUser;

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

        userRepository = new UserRepository(sessionFactory);
        userRepository.getEntityManager().getTransaction().begin();

        notExistUser = User.builder()
                .userName("UserUser")
                .password("445566")
                .role(Role.USER)
                .build();

        existUser = User.builder()
                .userId(2L)
                .userName("User")
                .password("1234")
                .role(Role.USER)
                .build();
    }

    @AfterEach
    public void resetTestBase(){
        userRepository.getEntityManager().getTransaction().commit();
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

    /* Начинаем тесты */

    @Test
    @DisplayName("1 - create User - Should return generated user ID")
    void shouldReturnCreatedUserIdTest() {
        Long generateId = userRepository.create(notExistUser).getUserId();
        assertThat(generateId).isNotNull();
    }

    /*
    Теоретически нужно бы провести тесты на попытку создать уже существующего пользователя,
    однако, на слое сервисов или на слое контроллеров (в зависимости от привычек разработчика),
    перед запросом метода *.create() будет проведена проверка или отсев методом *.findById(),
    который будет тестироваться ниже.
    */

    @Test
    @DisplayName("2 - findById User - Should return existent user")
    void shouldReturnOptionalUser_findByIdUserTest() {
        Long existingUserId = existUser.getUserId();
        Optional<User> mayBeUser = userRepository.findById(existingUserId);

        assertThat(mayBeUser).isPresent();
        assertAll(
                () -> assertThat(mayBeUser.get().getUserId()).isEqualTo(existingUserId),
                () -> assertThat(mayBeUser.get().getUserName()).isEqualTo(existUser.getUserName()),
                () -> assertThat(mayBeUser.get().getPassword()).isEqualTo(existUser.getPassword()),
                () -> assertThat(mayBeUser.get().getRole()).isEqualTo(existUser.getRole())
        );
    }

    @Test
    @DisplayName("3 - findById User - Should return empty Optional")
    void shouldReturnOptionalEmpty_findByIdUserTest() {
        Long nonExistentUserId = 20L;
        Optional<User> mayBeUser = userRepository.findById(nonExistentUserId);

        assertThat(mayBeUser).isEmpty();
    }

    @Test
    @DisplayName("4 - update User - Should return update user")
    void shouldReturnTrue_updateExistUserTest() {
        Long userId = 2L;
        existUser.setUserId(userId);
        String newName = "UpdateUserName";
        existUser.setUserName(newName);
        String newPassword = "654321";
        existUser.setPassword(newPassword);
        existUser.setRole(Role.ADMIN);

        userRepository.update(existUser);
        User isUpdateUser = userRepository.findById(userId).get();

        assertAll(
                () -> assertThat(isUpdateUser.getUserName()).isEqualTo(existUser.getUserName()),
                () -> assertThat(isUpdateUser.getPassword()).isEqualTo(existUser.getPassword()),
                () -> assertThat(isUpdateUser.getRole()).isEqualTo(existUser.getRole())
        );
    }

    @Test
    @DisplayName("5 - delete User - Should return true if user is deleted")
    void shouldReturnTrue_deleteUserTest() {
        Long existUserId = 4L;
        userRepository.delete(existUserId);

        assertThat(userRepository.findById(existUserId)).isEmpty();
    }

    @Test
    @DisplayName("6 - findAll Users - Should return size of user list")
    void shouldReturnListSize_findAllUserTest() {
        Integer listSize = userRepository.findAll().size();
        assertThat(listSize).isEqualTo(4);
    }

    @Test
    @DisplayName("7 - findUserByLogin - Should return true if user with current login exist")
    void shouldReturnTrue_findUserByLoginTest() {
        Optional<User> mayBeUser = userRepository.findUserByLogin(existUser.getUserName());
        assertThat(mayBeUser).isPresent();
    }

    @Test
    @DisplayName("8 - findUserByLogin - Should return false if user non existent")
    void shouldReturnFalse_notFindUserByLoginTest() {
        Optional<User> mayBeUser = userRepository.findUserByLogin("Batman");
        assertThat(mayBeUser).isEmpty();
    }

    @Test
    @DisplayName("9 - findUserByLoginAndPassword - Should return true if user with current login and pass exist")
    void shouldReturnTrue_findUserByLoginAndPasswordTest() {
        Optional<User> mayBeUser =
                userRepository.findUserByLoginAndPassword(existUser.getUserName(), existUser.getPassword());
        assertThat(mayBeUser).isPresent();
    }

    @Test
    @DisplayName("10 - findUserByLoginAndPassword - Should return false if user non existent")
    void shouldReturnFalse_notFindUserByLoginAndPasswordTest() {
        Optional<User> mayBeUser = userRepository.findUserByLoginAndPassword("Batman", "man_in_black");
        assertThat(mayBeUser).isEmpty();
    }
}