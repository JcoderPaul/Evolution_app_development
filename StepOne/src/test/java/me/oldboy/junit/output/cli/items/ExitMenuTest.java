package me.oldboy.junit.output.cli.items;

import me.oldboy.output.cli.items.ExitMenu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExitMenuTest {

    private static ExitMenu menu;
    private static boolean repeatMenu;
    private static Scanner scanner;
    private static ByteArrayInputStream in;

    @BeforeEach
    public void startInit(){
        repeatMenu = true;
        menu = new ExitMenu();
    }

    @Test
    public void exitMenu_Yes_Test() {
        in = new ByteArrayInputStream("Yes".getBytes());
        scanner = new Scanner(in);
        assertTrue(menu.exitMenu(scanner, repeatMenu));
    }


    @Test
    public void exitMenu_yes_Test() {
        in = new ByteArrayInputStream("yes".getBytes());
        scanner = new Scanner(in);
        assertTrue(menu.exitMenu(scanner, repeatMenu));
    }

    @Test
    public void exitMenu_No_Test() {
        in = new ByteArrayInputStream("No".getBytes());
        scanner = new Scanner(in);
        assertFalse(menu.exitMenu(scanner, repeatMenu));
    }

    @Test
    public void exitMenu_no_Test() {
        in = new ByteArrayInputStream("no".getBytes());
        scanner = new Scanner(in);
        assertFalse(menu.exitMenu(scanner, repeatMenu));
    }

    @Test
    public void exitMenu_BlaBla_AndYesAfterTest() throws IOException {
        in = new ByteArrayInputStream("BlaBla\nYes".getBytes());
        ByteArrayOutputStream outToScreen = new ByteArrayOutputStream();
        scanner = new Scanner(in);
        System.setOut(new PrintStream(outToScreen));
        assertTrue(menu.exitMenu(scanner, repeatMenu));
        outToScreen.flush();
        String allWrittenLines = new String(outToScreen.toByteArray());
        assertTrue(allWrittenLines.contains("Продолжить работу (Yes/No): " +
                                            "Неверные символы, будьте внимательны!"));
    }

    @Test
    public void exitMenu_BlaBla_AndNoAfterTest() throws IOException {
        in = new ByteArrayInputStream("BlaBla\nNo".getBytes());
        ByteArrayOutputStream outToScreen = new ByteArrayOutputStream();
        scanner = new Scanner(in);
        System.setOut(new PrintStream(outToScreen));
        assertFalse(menu.exitMenu(scanner, repeatMenu));
        outToScreen.flush();
        String allWrittenLines = new String(outToScreen.toByteArray());
        assertTrue(allWrittenLines.contains("Продолжить работу (Yes/No): " +
                                             "Неверные символы, будьте внимательны!"));
    }
}