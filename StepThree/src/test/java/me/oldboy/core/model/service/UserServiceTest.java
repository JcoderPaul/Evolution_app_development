package me.oldboy.core.model.service;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.dto.users.UserCreateDto;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.dto.users.UserUpdateDeleteDto;
import me.oldboy.exception.UserServiceException;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.core.model.database.repository.UserRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.validation.ConstraintViolationException;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService; // Тестируем методы данного класса
    private static SessionFactory sessionFactory;  // Связь Hibernate с БД
    private static Connection connection;  // Связь с БД (тестовой)
    private static LiquibaseManager liquibaseManager; // Подключаем стороннее управление БД
    private UserCreateDto notExistUser, existUserDto;
    private UserUpdateDeleteDto updateUserData;
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
        userService = new UserService(userRepository);

        userRepository.getEntityManager().getTransaction().begin();

        notExistUser = new UserCreateDto("UserUser", "445566", "USER");
        updateUserData = new UserUpdateDeleteDto(3L,"IsUpdatedUser", "998877", "ADMIN");
        existUserDto = new UserCreateDto("User", "1234", "USER");

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

    /* Блок тестов - выделим каждую группу тестов, если это необходимо, в отдельный вложенный класс, для удобства */

    /* Тестируем метод .findById() */

    @Nested
    @DisplayName("1 - UserService class *.findById method tests")
    class FindByIdMethodTests {

        @Test
        void shouldReturnUserReadDtoOfExistUser_findByIdTest() {
            Optional<UserReadDto> mayBeUser = userService.findById(2L);
            assertThat(mayBeUser).isPresent();
        }

        @Test
        void shouldReturnFalseOrOptionalNull_findByIdTest() {
            Optional<UserReadDto> mayBeUser = userService.findById(20L);
            assertThat(mayBeUser).isEmpty();
        }
    }

    /* Тестируем метод .create() */
    @Test
    @DisplayName("2 - UserService class *.create method test")
    void shouldReturnUserId_CreateUserTest() {
        Long generatedId = userService.create(notExistUser);
        assertThat(generatedId).isNotZero();

        Optional<UserReadDto> mayBeUser = userService.findById(generatedId);
        assertThat(mayBeUser).isPresent();

        assertAll(
                () -> assertThat(mayBeUser.get().userId()).isEqualTo(generatedId),
                () -> assertThat(mayBeUser.get().userName()).isEqualTo(notExistUser.userName()),
                () -> assertThat(mayBeUser.get().role().name()).isEqualTo(notExistUser.role())
        );
    }

    /* Тестируем метод .delete() */

    @Nested
    @DisplayName("3 - UserService class *.delete method tests")
    class DeleteMethodTests {

        @Test
        void shouldReturnTrueIfDeleteExistentUser_DeleteUserTest() {
            Boolean isUserDelete = userService.delete(existUser.getUserId());
            assertThat(isUserDelete).isTrue();
            assertThat(userService.findById(existUser.getUserId())).isEmpty();
        }

        @Test
        void shouldReturnFalseIfDeleteNonExistentUser_DeleteUserTest() {
            Boolean isUserDelete = userService.delete(40L);
            assertThat(isUserDelete).isFalse();
        }
    }

    /* Тестируем метод .update() */

    @Nested
    @DisplayName("4 - UserService class *.update method tests")
    class UpdateMethodTests {

        @Test
        void shouldReturnTrueIfUpdatedExistentUser_UpdateUserTest() {
            Boolean isUserUpdate = userService.update(updateUserData.userId(), updateUserData);
            assertThat(isUserUpdate).isTrue();
        }

        @Test
        void shouldReturnFalseIfTryToUpdateNonExistentUser_UpdateUserTest() {
            Boolean isUserUpdate = userService.update(40L, updateUserData);
            assertThat(isUserUpdate).isFalse();
        }

        @Test
        void shouldThrowExceptionNotValidData_UpdateUserTest() {
            UserUpdateDeleteDto notValidUserData = new UserUpdateDeleteDto(3L, "Hi", "32", "USER");
            assertThatThrownBy(() ->
                    userService.update(notValidUserData.userId(), notValidUserData))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("Wrong format (to short/to long)");
        }
    }

    /* Тестируем метод .findAll() */

    @Test
    @DisplayName("5 - UserService class *.findAll method test")
    void shouldReturnSizeOfUserReadDtoList_findAllUserTest() {
        List<UserReadDto> listOfUserReadDto = userService.findAll();
        assertThat(listOfUserReadDto.size()).isEqualTo(4);
    }

    /* Тестируем метод .findByUserName() */

    @Nested
    @DisplayName("6 - UserService class *.findByUserName method tests")
    class FindByUserNameMethodTests {

        @Test
        void shouldReturnUserReadDto_findByUserNameTest() {
            Optional<UserReadDto> mayBeUser = userService.findByUserName(existUser.getUserName());
            assertThat(mayBeUser).isPresent();
            assertAll(
                    () -> assertThat(mayBeUser.get().userId()).isEqualTo(existUser.getUserId()),
                    () -> assertThat(mayBeUser.get().userName()).isEqualTo(existUser.getUserName()),
                    () -> assertThat(mayBeUser.get().role()).isEqualTo(existUser.getRole())
            );
        }

        @Test
        void shouldReturnFalseOrEmptyUser_findByUserNameTest() {
            Optional<UserReadDto> mayBeUser = userService.findByUserName("I am Batman");
            assertThat(mayBeUser).isEmpty();
        }
    }

    /* Тестируем метод .findByUserNameAndPassword() */

    @Nested
    @DisplayName("7 - UserService class *.findByUserNameAndPassword method tests")
    class FindByUserNameAndPasswordMethodTests {
        @Test
        void shouldReturnUserReadDto_findByUserNameAndPasswordTest() {
            Optional<UserReadDto> mayBeUser =
                    userService.findByUserNameAndPassword(existUser.getUserName(), existUser.getPassword());
            assertThat(mayBeUser).isPresent();
            assertAll(
                    () -> assertThat(mayBeUser.get().userId()).isEqualTo(existUser.getUserId()),
                    () -> assertThat(mayBeUser.get().userName()).isEqualTo(existUser.getUserName()),
                    () -> assertThat(mayBeUser.get().role()).isEqualTo(existUser.getRole())
            );
        }

        @Test
        void shouldThrowException_findNonExistentUserByUserNameAndPasswordTest() {
            assertThatThrownBy(() -> userService.findByUserNameAndPassword("wolverine", "claws"))
                    .isInstanceOf(UserServiceException.class)
                    .hasMessageContaining("Password or login is incorrect! Вы ввели неверный пароль или логин!");
        }
    }
}