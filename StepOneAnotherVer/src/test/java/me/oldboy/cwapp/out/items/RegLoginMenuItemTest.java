package me.oldboy.cwapp.out.items;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.handlers.UserAuthenticationHandler;
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

class RegLoginMenuItemTest {

    @Mock
    private UserAuthenticationHandler userAuthenticationHandler;
    @InjectMocks
    private RegLoginMenuItem regLoginMenuItem;
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
    void regAndLogin() throws IOException {
        String userChoice = "1\n2\n3";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        scanner = new Scanner(inScanner);

        outScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outScreen));

        when(userAuthenticationHandler.registrationUser(scanner)).thenReturn(true);
        when(userAuthenticationHandler.loginUser(scanner)).thenReturn(userId);

        assertThat(regLoginMenuItem.regAndLogin(scanner)).isEqualTo(userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("Выберите один из пунктов меню: " +
                "\n1 - регистрация;" +
                "\n2 - вход в систему;" +
                "\n3 - покинуть программу;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }
}