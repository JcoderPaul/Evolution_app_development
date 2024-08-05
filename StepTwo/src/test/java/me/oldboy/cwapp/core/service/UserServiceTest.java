package me.oldboy.cwapp.core.service;

import me.oldboy.cwapp.exceptions.services.UserServiceException;
import me.oldboy.cwapp.core.entity.Reservation;
import me.oldboy.cwapp.core.entity.User;
import me.oldboy.cwapp.core.repository.crud.ReservationRepository;
import me.oldboy.cwapp.core.repository.crud.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private UserService userService;

    private User testUser;
    private Long userRegId;
    private String testLogin;
    private String testPass;

    @BeforeEach
    void setUp() {
        userRegId = 10L;
        testLogin = "Aerdoll";
        testPass = "forestgreen";
        testUser = new User(testLogin, testPass);

        MockitoAnnotations.openMocks(this);
    }

    /* Тестируем метод *.createUser() условного уровня сервисов */

    @Test
    void shouldReturnUserIfCreationSuccess_createUserTest() {
        when(userRepository.findUserByLogin(testUser.getLogin())).thenReturn(Optional.empty());
        when(userRepository.createUser(testUser)).thenReturn(Optional.of(testUser));
        assertThat(userService.createUser(testUser)).isEqualTo(testUser.getUserId());
    }

    @Test
    void shouldReturnExceptionIfCreationFail_createUserTest() {
        when(userRepository.findUserByLogin(testUser.getLogin())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(()->userService.createUser(testUser))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Пользователь не создан, логин '" +
                                                testUser.getLogin() +
                                                "' существует в системе!");
    }

    /* Тестируем метод *.findAllUser() условного уровня сервисов */

    @Test
    void shouldReturnUserList_findAllUserTest() {
        when(userRepository.findAllUsers()).thenReturn(List.of(new User(), new User()));
        assertThat(userService.findAllUser().size()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyList_findAllUserTest() {
        when(userRepository.findAllUsers()).thenReturn(List.of());
        assertThatThrownBy(() -> userService.findAllUser())
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("База пользователей пуста!");
    }

    /* Тестируем метод *.findAllUser() условного уровня сервисов */

    @Test
    void shouldReturnFindUser_findUserByIdTest() {
        testUser.setUserId(userRegId);
        when(userRepository.findUserById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        assertThat(userService.findUserById(userRegId)).isEqualTo(testUser);
    }

    @Test
    void shouldReturnExceptionIfUserNotExist_findUserByIdTest() {
        testUser.setUserId(userRegId);
        when(userRepository.findUserById(testUser.getUserId())).thenReturn(Optional.empty());

        assertThatThrownBy(()-> userService.findUserById(userRegId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Пользователь с ID - " + testUser.getUserId() + " не найден!");
    }

    /* Тестируем метод *.findUserByLogin() условного уровня сервисов */

    @Test
    void shouldReturnFindUserByLogin_findUserByLoginTest() {
        when(userRepository.findUserByLogin(testLogin)).thenReturn(Optional.of(testUser));
        assertThat(userService.findUserByLogin(testUser.getLogin())).isEqualTo(testUser);
    }

    @Test
    void shouldReturnExceptionFindIsFail_findUserByLoginTest() {
        when(userRepository.findUserByLogin(testLogin)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findUserByLogin(testUser.getLogin()))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Пользователь с login-ом: '" + testUser.getLogin() + "' не найден!");
    }

    /* Тестируем метод *.findUserByLoginAndPassword() условного уровня сервисов */

    @Test
    void shouldReturnFindUser_findUserByLoginAndPasswordTest() {
        when(userRepository.findUserByLoginAndPassword(testLogin, testPass)).thenReturn(Optional.of(testUser));
        assertThat(userService.findUserByLoginAndPassword(testLogin, testPass)).isEqualTo(testUser);
    }

    @Test
    void shouldReturnExceptionIfTryToFindNonExistentUser_findUserByLoginAndPasswordTest() {
        when(userRepository.findUserByLoginAndPassword(testLogin, testPass)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findUserByLoginAndPassword(testLogin, testPass))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Пользователь с таким паролем и логином не " +
                                                "найден или данные введены не верно");
    }

    /* Тестируем метод *.updateUser() условного уровня сервисов */

    @Test
    void shouldReturnTrue_updateUserTest() {
        User updateUser = new User(userRegId, "updateLogin", "updatePass");
        when(userRepository.findUserById(userRegId)).thenReturn(Optional.of(testUser));
        when(userRepository.updateUser(updateUser)).thenReturn(true);
        assertThat(userService.updateUser(updateUser)).isTrue();
    }

    @Test
    void shouldReturnExceptionIfUpdateNonExistentUser_updateUserTest() {
        User updateUser = new User(userRegId, "updateLogin", "updatePass");
        when(userRepository.findUserById(userRegId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(updateUser))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Пользователь с ID - " +
                                                updateUser.getUserId() + " в системе не найден, " +
                                                "обновление данных невозможно");
    }

    /* Тестируем метод *.deleteUser() условного уровня сервисов */

    @Test
    void shouldReturnTrue_deleteUserTest() {
        testUser.setUserId(userRegId);
        when(userRepository.findUserById(userRegId)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findReservationByUserId(userRegId)).thenReturn(Optional.of(List.of()));
        when(userRepository.deleteUser(userRegId)).thenReturn(true);
        assertThat(userService.deleteUser(testUser)).isTrue();
    }

    @Test
    void shouldReturnFalseIfDeleteNonExistentUser_deleteUserFailTest() {
        testUser.setUserId(userRegId);
        when(userRepository.findUserById(userRegId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteUser(testUser))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Пользователь с ID - " +
                                                testUser.getUserId() + " в системе не найден." +
                                                "Или у него есть зарезервированные " +
                                                "рабочие места или залы. Снимите бронь!");
    }

    @Test
    void shouldReturnFalseIfDeleteUserWithReservation_deleteUserFailTest() {
        testUser.setUserId(userRegId);
        when(userRepository.findUserById(userRegId))
                .thenReturn(Optional.of(testUser));
        when(reservationRepository.findReservationByUserId(userRegId))
                .thenReturn(Optional.of(List.of(new Reservation())));
        assertThatThrownBy(() -> userService.deleteUser(testUser))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Пользователь с ID - " +
                                                testUser.getUserId() + " в системе не найден." +
                                                "Или у него есть зарезервированные " +
                                                "рабочие места или залы. Снимите бронь!");
    }
}