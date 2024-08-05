package me.oldboy.cwapp.core.controllers;

import me.oldboy.cwapp.exceptions.controllers.UserControllerException;
import me.oldboy.cwapp.core.entity.User;
import me.oldboy.cwapp.core.service.UserService;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;
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
        testLogin = "NewUser";
        testPassword = "1234";
        testUser = new User(testLogin, testPassword);

        MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    public static void systemOutRestoringSettings() {
        System.setOut(System.out);
    }

    /* Тестируем метод *.registrationUser() условного уровня контроллеров */

    @Test
    void shouldReturnTrue_registrationUserTest() throws IOException {
        /*
        Формируем строки ввода, сначала в консоли идет приглашение
        ввести логин - имитируем ввод, затем следует приглашение
        ввести пароль - имитируем ввод пароля
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

        assertThat(userController.registrationUser(scanner)).isTrue();
        verify(userService, times(1)).createUser(any(User.class));

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
    void shouldReturnExceptionRepeatRegistrationUser_registrationUserTest() throws IOException {
        /*
        Формируем строки ответного ввода, сначала в консоли идет приглашение ввести логин -
        имитируем ввод, затем следует приглашение ввести пароль - имитируем ввод пароля
        */
        String makeLoginPasswordLine = testLogin + "\n" + testPassword;

        /* Преобразуем подготовленную строку в байтовый входящий поток */
        inToScanner = new ByteArrayInputStream(makeLoginPasswordLine.getBytes());
        scanner = new Scanner(inToScanner); // Отправляем в сканер подготовленную строку запроса

        when(userService.createUser(any(User.class))).thenReturn(null);
        assertThatThrownBy(()->userController.registrationUser(scanner))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Вероятно логин: " + testLogin + " уже занят!");

        /* Проверяем сколько раз был запущен метод *.createUser() класса UserService */
        verify(userService, times(1)).createUser(any(User.class));
    }

    /* Тестируем метод *.loginUser() условного уровня контроллеров */

    @Test
    void shouldReturnUserId_loginUserTest() throws IOException {
        /* Подготавливаем тестового user-a */
        testUser.setUserId(testUserId);

        /*
        Формируем строки ответного ввода, сначала в консоли идет приглашение ввести логин -
        имитируем ввод, затем следует приглашение ввести пароль - имитируем ввод пароля
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

        when(userService.findUserByLoginAndPassword(testLogin, testPassword)).thenReturn(testUser);
        assertThat(userController.loginUser(scanner)).isEqualTo(testUserId);
        verify(userService, times(1)).findUserByLoginAndPassword(testLogin, testPassword);

        /*
        Сбрасываем и записываем все буферизированные данные исходящего потока, преобразуем их в строку и
        смотрим наличие ожидаемой строки в преобразованном исходящем потоке в случае успеха выполнения метода
        */
        outStringData.flush();
        String allWrittenLines = new String(outStringData.toByteArray());
        assertTrue(allWrittenLines.contains("Вы вошли в систему!"));
    }

    @Test
    void shouldReturnExceptionLoginOrPasswordNonExistOrWrongEnterData_loginUserTest() throws IOException {
        /*
        Формируем строки ответного ввода, сначала в консоли идет приглашение ввести логин -
        имитируем ввод, затем следует приглашение ввести пароль - имитируем ввод пароля
        */
        String makeLoginPasswordLine = testLogin + "\n" + testPassword;

        /* Преобразуем подготовленную строку в байтовый входящий поток */
        inToScanner = new ByteArrayInputStream(makeLoginPasswordLine.getBytes());

        scanner = new Scanner(inToScanner); // Отправляем в сканер подготовленную строку запроса

        when(userService.findUserByLoginAndPassword(testLogin, testPassword)).thenReturn(null);
        assertThatThrownBy(() -> userController.loginUser(scanner))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Вероятно логин/пароль введены неверно!");

        verify(userService, times(1)).findUserByLoginAndPassword(testLogin, testPassword);
    }
}