package me.oldboy.junit.output.view;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.User;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import me.oldboy.output.view.AllReserveWithFilterView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for ReadAllReserveWithFilterDto.
 */
class AllReserveWithFilterViewTest {

    private static ReserveBase reserveBase;
    private static UserBase userBase;
    private static AllReserveWithFilterView readAllReserveWithFilterDto;
    private static User testUser;

    @BeforeEach
    public void setUp() {
        CoworkingContext.getInstance();
        userBase = CoworkingContext.getUserBase();
        readAllReserveWithFilterDto = new AllReserveWithFilterView();
        testUser = userBase.getUsersBase().get("Admin");
    }

    @AfterEach
    public void killBaseAfterAll() {
        userBase.getUsersBase()
                .get("Admin")
                .getUserReservedUnitList()
                .clear();

    }

    @Test
    void viewAllReserveSlotsByDate() throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo));
        readAllReserveWithFilterDto.viewAllReserveSlotsByDate(LocalDate.of(1999,05,05));
        bo.flush();
        String allWrittenLines = new String(bo.toByteArray());
        assertTrue(allWrittenLines.contains("зарезервированных залов и рабочих мест нет!"));
    }

    @Test
    void viewAllReserveByUser() throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo));
        readAllReserveWithFilterDto.viewAllReserveByUser(testUser);
        bo.flush();
        String allWrittenLines = new String(bo.toByteArray());
        assertTrue(allWrittenLines.contains("У пользователя Admin нет броней!"));
    }

    @Test
    void viewAllReserveHall() throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo));
        readAllReserveWithFilterDto.viewAllReserveHall();
        bo.flush();
        String allWrittenLines = new String(bo.toByteArray());
        assertTrue(allWrittenLines.contains("На данный момент нет ни одного зарезервированного зала!"));
    }

    @Test
    void viewAllReserveWorkplace() throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo));
        readAllReserveWithFilterDto.viewAllReserveWorkplace();
        bo.flush();
        String allWrittenLines = new String(bo.toByteArray());
        assertTrue(allWrittenLines.contains("На данный момент нет ни одного зарезервированного рабочего места!"));

    }
}