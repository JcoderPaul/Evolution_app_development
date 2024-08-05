package me.oldboy.cwapp.cui.items;

import me.oldboy.cwapp.core.controllers.UserController;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserAuthMenuItemTest {

    @Mock
    private UserController userController;
    @InjectMocks
    private UserAuthMenuItem userAuthMenuItem;
    private Long userId;
    private Scanner scanner;
    private ByteArrayOutputStream outScreen;
    private ByteArrayInputStream inScanner;

    @BeforeEach
    public void SetUp(){
        userId = 1L;
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void regAndLoginTest() throws IOException {
        String userChoice = "1\n2\n3";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        scanner = new Scanner(inScanner);

        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(userController.registrationUser(scanner)).thenReturn(true);
        when(userController.loginUser(scanner)).thenReturn(userId);

        assertThat(userAuthMenuItem.regAndLogin(scanner)).isEqualTo(userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("Выберите один из пунктов меню: " +
                "\n1 - регистрация;" +
                "\n2 - вход в систему;" +
                "\n3 - покинуть программу;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }
}