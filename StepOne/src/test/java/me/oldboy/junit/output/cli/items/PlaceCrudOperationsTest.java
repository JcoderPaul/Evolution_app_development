package me.oldboy.junit.output.cli.items;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.HallBaseException;
import me.oldboy.input.exeptions.WorkplaceBaseException;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.cli.items.PlaceCrudOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class PlaceCrudOperationsTest {

    private static PlaceCrudOperations placeCrudOperations;
    private static Scanner scanner;
    private static ByteArrayInputStream inToScanner;
    private static HallBase hallBase;
    private static WorkplaceBase workplaceBase;
    private static User user;
    private static Integer testPlace;
    private static Integer updateNumberTo;
    private static Integer crudMenuItem;
    private static ByteArrayOutputStream outStringData;

    @BeforeEach
    public void startInit(){
        CoworkingContext.getInstance();
        hallBase = CoworkingContext.getHallBase();
        workplaceBase = CoworkingContext.getWorkplaceBase();
        placeCrudOperations = new PlaceCrudOperations();
        user = CoworkingContext.getUserBase()
                               .login("Admin")
                               .get();
    }

    /* Тесты для workplaceBase */

    @Test
    @DisplayName("Should return create place - checking equal place number " +
                 "from base and number for test place creation")
    void crudOperationForWorkplacesCreateWorkplaceTest() throws IOException {
        testPlace = 11;
        crudMenuItem = 1;
        String makeCommandLine = crudMenuItem + " " + testPlace;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        placeCrudOperations.crudOperationForWorkplaces(user, scanner);
        assertEquals(workplaceBase.getWorkplaceBase().get(testPlace).getNumber(), testPlace);
    }

    @Test
    @DisplayName("Should return exception - try to create an existing place")
    void crudOperationForWorkplacesCreateExistingWorkplaceTest(){
        testPlace = 5;
        crudMenuItem = 1;
        String makeCommandLine = crudMenuItem + " " + testPlace;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        assertThrows(WorkplaceBaseException.class,
                     ()->placeCrudOperations.crudOperationForWorkplaces(user, scanner));
    }

    @Test
    @DisplayName("Should return false - after delete an existing place from base")
    void crudOperationForWorkplacesRemoveExistingWorkplaceTest(){
        testPlace = 10;
        crudMenuItem = 2;
        String makeCommandLine = crudMenuItem + " " + testPlace;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        placeCrudOperations.crudOperationForWorkplaces(user, scanner);
        assertFalse(workplaceBase.getWorkplaceBase().containsKey(testPlace));
    }

    @Test
    @DisplayName("Should return exception - delete not exist place")
    void crudOperationForWorkplacesRemoveNonExistingWorkplaceTest(){
        testPlace = 12;
        crudMenuItem = 2;
        String makeCommandLine = crudMenuItem + " " + testPlace;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        assertThrows(WorkplaceBaseException.class,
                     ()->placeCrudOperations.crudOperationForWorkplaces(user, scanner));
    }

    @Test
    @DisplayName("Should return update place - checking equal place number " +
                 "from base and number for update place number")
    void crudOperationForWorkplacesUpdateWorkplaceTest() throws IOException {
        testPlace = 8;
        crudMenuItem = 3;
        updateNumberTo = 15;
        String makeCommandLine = crudMenuItem + " " + testPlace + "\n" + updateNumberTo;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        placeCrudOperations.crudOperationForWorkplaces(user, scanner);
        assertEquals(workplaceBase.getWorkplaceBase().get(updateNumberTo).getNumber(), updateNumberTo);
    }

    @Test
    @DisplayName("Should return place string view  - checking equal place sting view " +
                 "from base and returning string view after 4-th menu item choice")
    void crudOperationForWorkplacesReadExistingWorkplaceTest() throws IOException {
        testPlace = 3; // Задаем рабочее место для чтения
        crudMenuItem = 4; // Выбираем пункт меню для тестирования
        String makeCommandLine = crudMenuItem + " " + testPlace; // Формируем строку запроса
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes()); // Отправляем строку запроса в байтовый входящий поток
        outStringData = new ByteArrayOutputStream(); // Создаем исходящий поток
        System.setOut(new PrintStream(outStringData)); // Задаем куда будет идти исходящий байтовый поток данных
        scanner = new Scanner(inToScanner); // Отправляем в сканер строку запроса
        placeCrudOperations.crudOperationForWorkplaces(user, scanner); // Запускаем тестируемый метод с заданными параметрами
        outStringData.flush(); // Сбрасываем и записываем все буферизированные данные исходящего потока
        String allWrittenLines = new String(outStringData.toByteArray()); // Преобразуем их в строку
        String findStringView = workplaceBase.getWorkplaceBase().get(testPlace).toString(); // Получаем данные того же рабочего места из базы
        assertTrue(allWrittenLines.contains(findStringView)); // Смотрим наличие сведений о рабочем места в преобразованном исходящем потоке
    }

    @Test
    @DisplayName("Should return exception  - after try to read not existing place")
    void crudOperationForWorkplacesReadNotExistingWorkplaceTest() throws IOException {
        testPlace = 25; // Задаем рабочее место для чтения
        crudMenuItem = 4; // Выбираем пункт меню для тестирования
        String makeCommandLine = crudMenuItem + " " + testPlace; // Формируем строку запроса
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes()); // Отправляем строку запроса в байтовый входящий поток
        scanner = new Scanner(inToScanner); // Отправляем в сканер строку запроса
        assertThrows(WorkplaceBaseException.class,
                ()->placeCrudOperations.crudOperationForWorkplaces(user, scanner));
    }

    /* Тесты для hallBase */

    @Test
    @DisplayName("Should return create place - checking equal hall number " +
                 "from base and number for test hall creation")
    void crudOperationForHallsCreateHallTest() throws IOException {
        testPlace = 4;
        crudMenuItem = 1;
        String makeCommandLine = crudMenuItem + " " + testPlace;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        placeCrudOperations.crudOperationForHalls(user, scanner);
        assertEquals(hallBase.getHallBase().get(testPlace).getNumber(), testPlace);
    }

    @Test
    @DisplayName("Should return exception - try to create an existing hall")
    void crudOperationForHallsCreateExistingHallTest(){
        testPlace = 1;
        crudMenuItem = 1;
        String makeCommandLine = crudMenuItem + " " + testPlace;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        assertThrows(HallBaseException.class,
                ()->placeCrudOperations.crudOperationForHalls(user, scanner));
    }

    @Test
    @DisplayName("Should return false - after delete an existing hall from base")
    void crudOperationForHallsRemoveExistingHallTest(){
        testPlace = 3;
        crudMenuItem = 2;
        String makeCommandLine = crudMenuItem + " " + testPlace;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        placeCrudOperations.crudOperationForHalls(user, scanner);
        assertFalse(hallBase.getHallBase().containsKey(testPlace));
    }

    @Test
    @DisplayName("Should return exception - delete not exist hall")
    void crudOperationForHallsRemoveNonExistingHallTest(){
        testPlace = 12;
        crudMenuItem = 2;
        String makeCommandLine = crudMenuItem + " " + testPlace;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        assertThrows(HallBaseException.class,
                ()->placeCrudOperations.crudOperationForHalls(user, scanner));
    }

    @Test
    @DisplayName("Should return update hall - checking equal hall number " +
                 "from base and number for update hall number")
    void crudOperationForHallsUpdateHallTest() throws IOException {
        testPlace = 2;
        crudMenuItem = 3;
        updateNumberTo = 5;
        String makeCommandLine = crudMenuItem + " " + testPlace + "\n" + updateNumberTo;
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes());
        scanner = new Scanner(inToScanner);
        placeCrudOperations.crudOperationForHalls(user, scanner);
        assertEquals(hallBase.getHallBase().get(updateNumberTo).getNumber(), updateNumberTo);
    }

    @Test
    @DisplayName("Should return hall string view  - checking equal hall sting view " +
                 "from base and returning string view after 4-th menu item choice")
    void crudOperationForHallsReadExistingHallTest() throws IOException {
        testPlace = 1; // Задаем зал для чтения
        crudMenuItem = 4; // Выбираем пункт меню для тестирования
        String makeCommandLine = crudMenuItem + " " + testPlace; // Формируем строку запроса
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes()); // Отправляем строку запроса в байтовый входящий поток
        outStringData = new ByteArrayOutputStream(); // Создаем исходящий поток
        System.setOut(new PrintStream(outStringData)); // Задаем куда будет идти исходящий байтовый поток данных
        scanner = new Scanner(inToScanner); // Отправляем в сканер строку запроса
        placeCrudOperations.crudOperationForHalls(user, scanner); // Запускаем тестируемый метод с заданными параметрами
        outStringData.flush(); // Сбрасываем и записываем все буферизированные данные исходящего потока
        String allWrittenLines = new String(outStringData.toByteArray()); // Преобразуем их в строку
        String findStringView = hallBase.getHallBase().get(testPlace).toString(); // Получаем данные того же зала из базы
        assertTrue(allWrittenLines.contains(findStringView)); // Смотрим наличие сведений о рабочем места в преобразованном исходящем потоке
    }

    @Test
    @DisplayName("Should return exception  - after try to read not existing hall")
    void crudOperationForHallsReadNotExistingHallTest() throws IOException {
        testPlace = 5; // Задаем рабочее место для чтения
        crudMenuItem = 4; // Выбираем пункт меню для тестирования
        String makeCommandLine = crudMenuItem + " " + testPlace; // Формируем строку запроса
        inToScanner = new ByteArrayInputStream(makeCommandLine.getBytes()); // Отправляем строку запроса в байтовый входящий поток
        scanner = new Scanner(inToScanner); // Отправляем в сканер строку запроса
        assertThrows(HallBaseException.class,
                ()->placeCrudOperations.crudOperationForHalls(user, scanner));
    }
}