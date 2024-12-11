package me.oldboy.core.controllers;

import me.oldboy.core.dto.users.UserCreateDto;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.dto.users.UserUpdateDeleteDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.core.model.service.SecurityService;
import me.oldboy.core.model.service.UserService;
import me.oldboy.exception.UserControllerException;
import me.oldboy.security.JwtAuthResponse;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private SecurityService securityService;
    @InjectMocks
    private UserController userController;

    private static String testLogin;
    private static String testPassword;
    private static Long testUserId;
    private static String testRole;
    private static UserCreateDto userCreateDto;
    private static UserReadDto userReadDto;
    private static UserUpdateDeleteDto userUpdateDeleteDto, notCorrectDataUserUpdateDeleteDto;
    private static JwtAuthResponse testJwtResponse;

    @BeforeAll
    public static void initParam(){
        testUserId = 200L;
        testLogin = "NewUser";
        testPassword = "1234";
        testRole = "USER";
        userCreateDto = new UserCreateDto(testLogin, testPassword, testRole);
        userReadDto = new UserReadDto(testUserId, testLogin, Role.USER);
        userUpdateDeleteDto = new UserUpdateDeleteDto(1L, "Admin", "1234", "ADMIN");
        notCorrectDataUserUpdateDeleteDto = new UserUpdateDeleteDto(17L, "Admin", "1234", "ADMIN");
        testJwtResponse = new JwtAuthResponse(testUserId, testLogin, Role.USER, "testToken");
    }

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    /* Блок тестов - выделим каждую группу тестов в отдельный вложенный класс, для удобства и наглядности */

    /* Метод *.registrationUser() */

    @Nested
    @DisplayName("1 - UserController class *.registrationUser method tests")
    class RegistrationUserMethodTests {

        @Test
        void shouldReturnTrue_registrationUserTest() {
            when(userService.findByUserName(testLogin)).thenReturn(Optional.empty());
            when(userService.create(userCreateDto)).thenReturn(testUserId);

            assertThat(userController.registrationUser(userCreateDto)).isTrue();

            verify(userService, times(1)).create(any(UserCreateDto.class));
            verify(userService, times(1)).findByUserName(any(String.class));
        }

        @Test
        void shouldThrowException_registrationExistUserTest() {
            when(userService.findByUserName(testLogin)).thenReturn(Optional.of(userReadDto));

            assertThatThrownBy(() -> userController.registrationUser(userCreateDto))
                    .isInstanceOf(UserControllerException.class)
                    .hasMessageContaining("User with name ' " + userCreateDto.userName() + " ' is already exist! " +
                            "Пользователь с именем ' " + userCreateDto.userName() + " ' уже существует!");

            verify(userService, times(1)).findByUserName(any(String.class));
        }
    }

    /* Метод *.loginUser() */

    @Nested
    @DisplayName("2 - UserController class *.loginUser method tests")
    class LoginUserMethodTests {

        @Test
        void shouldReturnJwtAuthResponse_loginUserTest() {
            when(userService.findByUserName(userCreateDto.userName()))
                    .thenReturn(Optional.of(userReadDto));
            when(securityService.loginUser(userCreateDto.userName(), userCreateDto.password()))
                    .thenReturn(testJwtResponse);

            assertThat(userController.loginUser(userCreateDto.userName(), userCreateDto.password()))
                    .isEqualTo(testJwtResponse);

            verify(userService, times(1)).findByUserName(anyString());
            verify(securityService, times(1)).loginUser(anyString(), anyString());
        }

        @Test
        void shouldThrowException_NotFoundUser_loginUserTest() {
            when(userService.findByUserName(userCreateDto.userName())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userController.loginUser(testLogin, testPassword))
                    .isInstanceOf(UserControllerException.class)
                    .hasMessageContaining("Login '" + testLogin + "' not found! " +
                            "Пользователь с логином '" + testLogin + "' не найден!");

            verify(userService, times(1)).findByUserName(anyString());
            verifyNoInteractions(securityService);
        }
    }

    /* Метод *.deleteUser() */

    @Nested
    @DisplayName("3 - UserController class *.deleteUser method tests")
    class DeleteUserMethodTests {

        @Test
        void shouldReturnTrueIfDeleteExistUser_deleteUserTest() {
            when(userService.findByUserNameAndPassword(userUpdateDeleteDto.userName(),
                    userUpdateDeleteDto.password()))
                    .thenReturn(Optional.of(new UserReadDto(userUpdateDeleteDto.userId(),
                            userUpdateDeleteDto.userName(),
                            Role.valueOf(userUpdateDeleteDto.role()))));
            when(userService.delete(userUpdateDeleteDto.userId())).thenReturn(true);

            assertThat(userController.deleteUser(userUpdateDeleteDto, "BigAdmin")).isTrue();

            verify(userService, times(1)).findByUserNameAndPassword(anyString(), anyString());
            verify(userService, times(1)).delete(anyLong());
        }

        @Test
        void shouldThrowExceptionIfDeleteNonExistentUser_deleteUserTest() {
            when(userService.findByUserNameAndPassword("testNonEx", "noPass")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userController.deleteUser(new UserUpdateDeleteDto(14L, "testNonEx", "noPass", "USER"), "BigAdmin"))
                    .isInstanceOf(UserControllerException.class)
                    .hasMessageContaining("Have no user to remove! Пользователь для удаления не найден");

            verify(userService, times(1)).findByUserNameAndPassword(anyString(), anyString());
        }

        @Test
        void shouldThrowExceptionIfDeleteExistentUserWithNonCongruentData_deleteUserTest() {
            when(userService.findByUserNameAndPassword(userUpdateDeleteDto.userName(), userUpdateDeleteDto.password()))
                    .thenReturn(Optional.of(new UserReadDto(userUpdateDeleteDto.userId(),
                            userUpdateDeleteDto.userName(),
                            Role.valueOf(userUpdateDeleteDto.role()))));

            assertThatThrownBy(() -> userController.deleteUser(notCorrectDataUserUpdateDeleteDto, "BigAdmin"))
                    .isInstanceOf(UserControllerException.class)
                    .hasMessageContaining("Data not congruent! Уверенны что хотите удалить именно этого пользователя?");

            verify(userService, times(1)).findByUserNameAndPassword(anyString(), anyString());
        }
    }

    /* Метод *.updateUser() */

    @Nested
    @DisplayName("4 - UserController class *.updateUser method tests")
    class UpdateUserMethodTests {

        @Test
        void shouldReturnTrueIfUpdateExistUser_updateUserTest() {
            when(userService.update(userUpdateDeleteDto.userId(), userUpdateDeleteDto)).thenReturn(true);

            assertThat(userController.updateUser(userUpdateDeleteDto, "BigAdmin")).isTrue();

            verify(userService, times(1)).update(anyLong(), any(UserUpdateDeleteDto.class));
        }

        @Test
        void shouldReturnFalseIfUpdateNotExistUser_updateUserTest() {
            when(userService.update(notCorrectDataUserUpdateDeleteDto.userId(), notCorrectDataUserUpdateDeleteDto))
                    .thenReturn(false);

            assertThat(userController.updateUser(userUpdateDeleteDto, "BigAdmin")).isFalse();

            verify(userService, times(1)).update(anyLong(), any(UserUpdateDeleteDto.class));
        }

        @Test
        void shouldReturnUserList_getAllUserTest() {
            when(userService.findAll()).thenReturn(List.of(new UserReadDto(5L, "test1", Role.ADMIN),
                    new UserReadDto(6L, "test2", Role.USER)));

            assertThat(userController.getAllUser().size() == 2).isTrue();

            verify(userService, times(1)).findAll();
        }
    }
}