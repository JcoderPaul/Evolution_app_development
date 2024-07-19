package me.oldboy.cwapp.out.items;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.handlers.PlaceViewHandler;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CrudOperationWithPlacesMenuItemTest {

    @Mock
    private PlaceViewHandler placeViewHandler;
    @InjectMocks
    private CrudOperationWithPlacesMenuItem crudOperationWithPlacesMenuItem;
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

        when(placeViewHandler.createNewPlace(scanner, userId)).thenReturn(1L);
        when(placeViewHandler.deletePlace(scanner, userId)).thenReturn(true);
        when(placeViewHandler.updatePlace(scanner, userId)).thenReturn(new Place());
        crudOperationWithPlacesMenuItem.makeOperation(scanner, userId);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("Выберите тип операции: " +
                "\n1 - создание рабочего места/конференц зала;" +
                "\n2 - удаление рабочего места/конференц зала;" +
                "\n3 - редактирование рабочего места/конференц зала;" +
                "\n4 - просмотр существующих рабочих мест и конференц залов; " +
                "\n5 - выход из текущего меню;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }
}