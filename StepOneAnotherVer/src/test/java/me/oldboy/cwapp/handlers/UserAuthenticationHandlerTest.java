package me.oldboy.cwapp.handlers;

import me.oldboy.cwapp.entity.Role;
import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.handlers_exception.UserAuthenticationHandlerException;
import me.oldboy.cwapp.handlers.UserAuthenticationHandler;
import me.oldboy.cwapp.services.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserAuthenticationHandlerTest {

    @Mock
    private UserService userService;
    @InjectMocks
    private UserAuthenticationHandler userViewHandler;
    private String testLogin;
    private String testPassword;
    private Long testUserId;
    private User testUser;
    private Scanner scanner;
    private ByteArrayInputStream inToScanner;
    private ByteArrayOutputStream outStringData;

    @BeforeEach
    public void setUp(){
        testUserId = 9L;
        testLogin = "User";
        testPassword = "1234";
        testUser = new User(testLogin, testPassword, Role.USER);

        MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    public static void systemOutRestoringSettings() {
        System.setOut(System.out);
    }

    @Test
    void shouldReturnTrue_registrationNonExistentUser() throws IOException {
        /*
        Формируем строки ответного ввода, сначала в консоли идет
        приглашение ввести логин - имитируем ввод, затем следует
        приглашение ввести пароль - имитируем ввод пароля
        */
        String makeLoginPasswordLine = testLogin + "\n" + testPassword;

        /* Преобразуем подготовленную строку в байтовый входящий поток */
        inToScanner = new ByteArrayInputStream(makeLoginPasswordLine.getBytes());

        /*
        В нашем тестируемом методе есть так же и некий вывод в консоль,
        две следующие строки задают куда будет возвращен исходящий поток
        */
        outStringData = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStringData));

        scanner = new Scanner(inToScanner); // Отправляем в сканер подготовленную строку запроса

        assertThat(userViewHandler.registrationUser(scanner)).isTrue();
        verify(userService, times(1)).registration(any(User.class));

        /*
        Сбрасываем и записываем все буферизированные данные исходящего
        потока, преобразуем их в строку и смотрим наличие ожидаемой строки
        в преобразованном исходящем потоке в случае успеха выполнения метода
        */
        outStringData.flush();
        String allWrittenLines = new String(outStringData.toByteArray());
        assertTrue(allWrittenLines.contains("Пользователь успешно зарегистрирован!"));
    }

    @Test
    void shouldReturnException_repeatedRegistrationExistentUser() throws IOException {
        /*
        Формируем строки ответного ввода, сначала в консоли идет
        приглашение ввести логин - имитируем ввод, затем следует
        приглашение ввести пароль - имитируем ввод пароля
        */
        String makeLoginPasswordLine = testLogin + "\n" + testPassword;

        /* Преобразуем подготовленную строку в байтовый входящий поток */
        inToScanner = new ByteArrayInputStream(makeLoginPasswordLine.getBytes());
        scanner = new Scanner(inToScanner); // Отправляем в сканер подготовленную строку запроса

        when(userService.registration(any(User.class))).thenReturn(null);
        assertThatThrownBy(()->userViewHandler.registrationUser(scanner))
                .isInstanceOf(UserAuthenticationHandlerException.class)
                .hasMessageContaining("Вероятно логин: " + testLogin + " уже занят!");

        verify(userService, times(1)).registration(any(User.class));
    }

    @Test
    void shouldReturnIdUser_loginExistUser() throws IOException {
        /* Подготавливаем тестового user-a */
        testUser.setUserId(testUserId);

        /*
        Формируем строки ответного ввода, сначала в консоли идет
        приглашение ввести логин - имитируем ввод, затем следует
        приглашение ввести пароль - имитируем ввод пароля
        */
        String makeLoginPasswordLine = testLogin + "\n" + testPassword;

        /* Преобразуем подготовленную строку в байтовый входящий поток */
        inToScanner = new ByteArrayInputStream(makeLoginPasswordLine.getBytes());

        /*
        В нашем тестируемом методе есть так же и некий вывод в консоль,
        две следующие строки задают куда будет возвращен исходящий поток
        */
        outStringData = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStringData));

        scanner = new Scanner(inToScanner); // Отправляем в сканер подготовленную строку запроса

        when(userService.login(testLogin, testPassword)).thenReturn(testUser);
        assertThat(userViewHandler.loginUser(scanner)).isEqualTo(testUserId);
        verify(userService, times(1)).login(testLogin, testPassword);

        /*
        Сбрасываем и записываем все буферизированные данные исходящего
        потока, преобразуем их в строку и смотрим наличие ожидаемой строки
        в преобразованном исходящем потоке в случае успеха выполнения метода
        */
        outStringData.flush();
        String allWrittenLines = new String(outStringData.toByteArray());
        assertTrue(allWrittenLines.contains("Вы вошли в систему!"));
    }

    @Test
    void shouldReturnException_loginNonExistOrWrongEnterDataUser() throws IOException {
        /*
        Формируем строки ответного ввода, сначала в консоли идет
        приглашение ввести логин - имитируем ввод, затем следует
        приглашение ввести пароль - имитируем ввод пароля
        */
        String makeLoginPasswordLine = testLogin + "\n" + testPassword;

        /* Преобразуем подготовленную строку в байтовый входящий поток */
        inToScanner = new ByteArrayInputStream(makeLoginPasswordLine.getBytes());

        scanner = new Scanner(inToScanner); // Отправляем в сканер подготовленную строку запроса

        when(userService.login(testLogin, testPassword)).thenReturn(null);
        assertThatThrownBy(()->userViewHandler.loginUser(scanner))
                .isInstanceOf(UserAuthenticationHandlerException.class)
                .hasMessageContaining("Вероятно логин/пароль введены неверно!");
        verify(userService, times(1)).login(testLogin, testPassword);
    }
}