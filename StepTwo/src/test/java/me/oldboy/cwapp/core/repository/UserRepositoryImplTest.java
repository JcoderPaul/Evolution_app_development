package me.oldboy.cwapp.core.repository;

import me.oldboy.cwapp.config.connection.ConnectionManager;
import me.oldboy.cwapp.config.liquibase.LiquibaseManager;
import me.oldboy.cwapp.core.entity.Role;
import me.oldboy.cwapp.core.entity.User;
import me.oldboy.cwapp.core.repository.crud.UserRepository;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/* Помечаем тестовый метод аннотацией намекающей, что тестирование пойдет через тест-контейнер */
@Testcontainers
class UserRepositoryImplTest {

    private UserRepository userRepository; // Тестируем методы данного интерфейса

    private Connection connection; // Связь с БД (рабочей/тестовой)

    private LiquibaseManager liquibaseManager = LiquibaseManager.getInstance(); // Подключаем стороннее управление БД

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
        userRepository = new UserRepositoryImpl(connection);
    }

    /*
    В данном методе запускается метод отката БД "до нулевого" состояния - вплоть до удаления всех таблиц,
    что крайне неэффективно, т.к. в предыдущем методе нам приходится эту БД накатывать с нуля, это
    ресурсоемкий процесс, т.к. эту процедуру мы делаем для каждого теста, эффективнее было бы, например,
    просто очистить только таблицы или просто в тестах откатывать изменения. Но мы тренируемся, по этому
    реализовано так.
    */
    @AfterEach
    public void resetTestBase(){
        liquibaseManager.rollbackCreatedTables(connection);
    }

    @AfterAll
    public static void stopTestContainer(){
        postgresContainer.stop();
    }

    /* Основные тесты для реализаций методов UserRepository */

    @Test
    @DisplayName("1 - Should return create optional user")
    void shouldReturnOptionalUserIfCreationIsSuccess_createUserTest() {
        User createNewUser = new User("Malkolm","stone");
        Optional<User> mayBeUser =
                userRepository.createUser(createNewUser);

        assertThat(mayBeUser.isPresent()).isTrue();

        assertAll(
                () -> assertThat(mayBeUser.get().getLogin()).isEqualTo("Malkolm"),
                () -> assertThat(mayBeUser.get().getPassword()).isEqualTo("stone")
        );
    }

    @Test
    @DisplayName("2 - Should return list of user")
    void shouldReturnListOfUser_findAllUsersTest() {
        List<User> userList = userRepository.findAllUsers();

        assertThat(userList.size()).isEqualTo(3);

        assertAll(
                () -> assertThat(userList.get(0).getLogin())
                        .isEqualTo("Admin"),
                () -> assertThat(userList.get(userList.size()-1).getLogin())
                        .isEqualTo("UserTwo")
        );
    }

    /* Тесты метода *.findUserById */

    @Test
    @DisplayName("3 - Should return optional user find by ID")
    void shouldReturnFindOptionalUser_findUserByIdTest() {
        Optional<User> findUser = userRepository.findUserById(1L);

        assertThat(findUser.isPresent()).isTrue();

        assertAll(
                () -> assertThat(findUser.get().getLogin()).isEqualTo("Admin"),
                () -> assertThat(findUser.get().getPassword()).isEqualTo("1234"),
                () -> assertThat(findUser.get().getRole()).isEqualTo(Role.ADMIN)
        );
    }

    @Test
    @DisplayName("4 - Should return Optional null/false to try find non existent user by ID")
    void shouldReturnOptionalNullTryFindNonExistentUser_findUserByIdTest() {
        Optional<User> findUser = userRepository.findUserById(10L);

        assertThat(findUser.isPresent()).isFalse();
    }

    /* Тесты метода *.findUserByLogin() */

    @Test
    @DisplayName("5 - Should return optional user find by login")
    void shouldReturnOptionalUser_findUserByLoginTest() {
        Optional<User> findUser = userRepository.findUserByLogin("UserTwo");

        assertThat(findUser.isPresent()).isTrue();

        assertAll(
                () -> findUser.get().getLogin().equals("UserTwo"),
                () -> findUser.get().getPassword().equals("1234"),
                () -> findUser.get().getRole().equals(Role.USER)
        );
    }

    @Test
    @DisplayName("6 - Should return optional null if not find user by login")
    void shouldReturnOptionalNullFindNonExistentLogin_findUserByLoginTest() {
        Optional<User> findMayBeUser = userRepository.findUserByLogin("Sanara");

        assertThat(findMayBeUser.isPresent()).isFalse();
    }

    /* Тесты метода *.findUserByLoginAndPassword() */

    @Test
    @DisplayName("7 - Should return optional user if find user by login and password")
    void shouldReturnTrueOptionalUser_findUserByLoginAndPasswordTest() {
        Optional<User> findMayBeUser =
                userRepository.findUserByLoginAndPassword("Admin", "1234");

        assertThat(findMayBeUser.isPresent()).isTrue();
    }

    @Test
    @DisplayName("8 - Should return false / optional null not find non existent user by login and password ")
    void shouldReturnFalseOptionalNull_findUserByLoginAndPasswordTest() {
        Optional<User> findMayBeUser =
                userRepository.findUserByLoginAndPassword("Sanara", "1212");

        assertThat(findMayBeUser.isPresent()).isFalse();
    }

    /* Тесты метода *.updateUserTest() */

    @Test
    @DisplayName("9 - Should return true if update user is success")
    void shouldReturnTrueIfUpdateSuccess_updateUserTest() {
        User updateUser = new User(1L, "Administrator", "big_admin");
        Boolean isUpdateGood = userRepository.updateUser(updateUser);

        assertThat(isUpdateGood).isTrue();

        assertAll(
                () -> assertThat(updateUser.getUserId())
                        .isEqualTo(userRepository.findUserById(1L).get().getUserId()),
                () -> assertThat(updateUser.getLogin())
                        .isEqualTo(userRepository.findUserById(1L).get().getLogin()),
                () -> assertThat(updateUser.getPassword())
                        .isEqualTo(userRepository.findUserById(1L).get().getPassword())
        );
    }

    @Test
    @DisplayName("10 - Should return false if update not existed user")
    void shouldReturnFalseIfUpdateFail_updateUserTest() {
        User updateUser = new User(10L, "Duglas", "lindi_mindi");
        Boolean isUpdateGood = userRepository.updateUser(updateUser);

        assertThat(isUpdateGood).isFalse();
    }

    /* Тесты метода *.deleteUser() */

    @Test
    @DisplayName("11 - Should return true if user is deleted success")
    void shouldReturnTrueIfDeletedSuccess_deleteUserTest() {
        userRepository.createUser(new User("Timus", "rody122"));
        Integer sizeOfListBeforeDeletePlace = userRepository.findAllUsers().size();
        boolean isDeleteGood = userRepository.deleteUser(4L);
        Integer sizeOfListAfterDeletePlace = userRepository.findAllUsers().size();

        assertThat(isDeleteGood).isTrue();

        assertThat(sizeOfListBeforeDeletePlace).isGreaterThan(sizeOfListAfterDeletePlace);
    }

    @Test
    @DisplayName("12 - Should return false if user is not deleted/non existent")
    void shouldReturnFalseIfDeleteFail_deleteUserTest() {
        Integer sizeOfListBeforeDeletePlace = userRepository.findAllUsers().size();
        boolean isDeleteGood = userRepository.deleteUser(12L);
        Integer sizeOfListAfterDeletePlace = userRepository.findAllUsers().size();

        assertThat(isDeleteGood).isFalse();
        assertThat(sizeOfListBeforeDeletePlace).isEqualTo(sizeOfListAfterDeletePlace);
    }
}