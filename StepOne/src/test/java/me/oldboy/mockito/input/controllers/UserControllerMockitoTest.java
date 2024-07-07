package me.oldboy.mockito.input.controllers;

import me.oldboy.input.controllers.UserController;
import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.UserControllerException;
import me.oldboy.input.repository.UserBase;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests for UserController.
 */
class UserControllerMockitoTest {
    @Mock
    private UserBase userBase;
    @InjectMocks
    private UserController userController;
    private static String godUserLogin;
    private static String emptyUserLogin;
    private static String alreadyExistUserLogin;

    @BeforeAll
    public static void setUp() {
        godUserLogin = "User";
        emptyUserLogin = "";
        alreadyExistUserLogin = "Admin";
    }

    @BeforeEach
    void beforeAllTests() {
        MockitoAnnotations.openMocks(this);
    }

    /* Проверяем создание пользователя */

    @Test
    @DisplayName("Should return true when user is create")
    public void goodCreateUserTest(){
        when(userBase.createUser(godUserLogin)).thenReturn(Optional.of(new User(godUserLogin)));
        assertThat(userController.createUser(godUserLogin)).isTrue();
    }

    @Test
    @DisplayName("Should return exception when user enter empty login to create-user form")
    public void emptyCreateLoginTest(){
        assertThatThrownBy(()->userController.createUser(emptyUserLogin))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Вы ничего не ввели");
    }

    @Test
    @DisplayName("Should return exception when user enter null login to create-user form")
    public void nullCreateLoginTest(){
        assertThatThrownBy(()->userController.createUser(null))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Вы ничего не ввели");
    }

    @Test
    @DisplayName("Should return false when try to create existing login")
    public void alreadyExistCreateLoginTest(){
        when(userBase.createUser(alreadyExistUserLogin)).thenReturn(Optional.empty());

        assertThat(userController.createUser(alreadyExistUserLogin)).isFalse();
    }

    /* Проверяем работу метода */

    @Test
    @DisplayName("Should return same login if user enter an existing login in user base")
    public void alreadyExistingUserLoginTest(){
        User userInBase = new User(alreadyExistUserLogin);
        when(userBase.login(alreadyExistUserLogin)).thenReturn(Optional.of(userInBase));
        User userResult = userController.login(alreadyExistUserLogin);

        assertThat(userResult).isEqualTo(userInBase);
    }

    @Test
    @DisplayName("Should return exception when try to enter not existing login")
    public void notExistingLoginUserTest(){
        when(userBase.login(godUserLogin)).thenReturn(Optional.empty());

        assertThatThrownBy(()->userController.login(godUserLogin))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Пользователя с таким логином НЕ существует!");
    }

    @Test
    @DisplayName("Should return exception when user enter empty login to login form")
    public void emptyEnterLoginTest(){
        assertThatThrownBy(()->userController.login(emptyUserLogin))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Логин не может быть пустым");

    }

    @Test
    @DisplayName("Should return exception when user enter null login to login form")
    public void nullEnterLoginTest(){
        assertThatThrownBy(()->userController.login(null))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Логин не может быть пустым");
    }
}