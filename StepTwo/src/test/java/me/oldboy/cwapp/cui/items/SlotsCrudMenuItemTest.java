package me.oldboy.cwapp.cui.items;

import me.oldboy.cwapp.core.controllers.SlotController;
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

class SlotsCrudMenuItemTest {

    @Mock
    private SlotController slotController;
    @InjectMocks
    private SlotsCrudMenuItem slotsCrudMenuItem;
    private Scanner scanner;
    private ByteArrayInputStream inScanner;
    private ByteArrayOutputStream outScreen;
    private Long userId;

    @BeforeEach
    public void setUp(){
        userId = 2L;
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void makeOperationTest() throws IOException {
        String userChoice = "1\n2\n3\n5";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));

        when(slotController.createNewSlot(scanner, userId)).thenReturn(1L);
        when(slotController.deleteSlot(scanner, userId)).thenReturn(true);
        when(slotController.updateSlot(scanner, userId)).thenReturn(true);

        slotsCrudMenuItem.makeOperation(scanner, userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("Выберите тип операции: " +
                "\n1 - создание слота;" +
                "\n2 - удаление слота;" +
                "\n3 - редактирование слота;" +
                "\n4 - просмотр существующих слотов; " +
                "\n5 - выход из текущего меню;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }
}