package me.oldboy.cwapp.services;

import me.oldboy.cwapp.store.base.UserBase;
import me.oldboy.cwapp.entity.Role;
import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.service_exception.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserBase userBase;
    @InjectMocks
    private UserService userService;

    private User testUser;
    private Long userRegId;
    private String testLogin;
    private String testPass;

    @BeforeEach
    void setUp() {
        userRegId = 1L;
        testLogin = "Seleznoff";
        testPass = "2124";
        testUser = new User(testLogin, testPass, Role.USER);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrationUserServiceGoodTest() {
        when(userBase.findUserByLogin(testUser.getUserLogin())).thenReturn(Optional.empty());
        when(userBase.create(testUser)).thenReturn(userRegId);
        assertThat(userService.registration(testUser)).isEqualTo(userRegId);
    }

    @Test
    void registrationUserServiceExceptionTest() {
        testUser.setUserId(userRegId);
        when(userBase.findUserByLogin(testUser.getUserLogin())).thenReturn(Optional.empty());
        assertThatThrownBy(()->userService.registration(testUser))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Логин: " + testUser.getUserLogin() +
                                                "уже есть в системе или " +
                                                "переданные данные содержат недопустимые значения, например ID!");
    }

    @Test
    void loginUserServiceGoodTest() {
        when(userBase.findUserByLogin(testUser.getUserLogin())).thenReturn(Optional.of(testUser));
        assertThat(userService.login(testLogin, testPass)).isEqualTo(testUser);
    }

    @Test
    void loginUserServiceNonRegistrationExceptionTest() {
        when(userBase.findUserByLogin("notExistLogin")).thenReturn(Optional.empty());
        assertThatThrownBy(()->userService.login("nonExistLogin", "nonExistPass"))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Пользователь не зарегистрирован!");
    }

    @Test
    void loginUserServiceWrongPassExceptionTest() {
        Long userRegId = 1L;
        String testLogin = "Seleznou";
        String testPass = "2124";
        User testUser = new User(userRegId, testLogin, testPass, Role.ADMIN);

        when(userBase.findUserByLogin(testUser.getUserLogin())).thenReturn(Optional.of(testUser));
        assertThatThrownBy(()->userService.login(testLogin, "nonExistPass"))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Неверный пароль!");
    }
}