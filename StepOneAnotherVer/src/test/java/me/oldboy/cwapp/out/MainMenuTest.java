package me.oldboy.cwapp.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MainMenuTest {

    private Scanner scanner;
    private ByteArrayInputStream inScanner;
    private ByteArrayOutputStream outScreen;
    private Long userId;

    @BeforeEach
    public void setUp(){
        MainMenu.getInstance();
    }

    @Test
    void startMainMenu() throws IOException {
        String userChoice = "2\nAdmin\nadmin\n1\n6\n2\n5\n3";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));

        MainMenu.startMainMenu(scanner);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("\nВыберите интересующий вас пункт меню: \n" +
                "1 - резервирование и просмотр рабочих мест и конференц-залов;\n" +
                "2 - управление рабочими местами и конференц-залами (только для ADMIN); \n" +
                "3 - покинуть программу;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }
}