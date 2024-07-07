package me.oldboy.junit.output.cli.items;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.cli.items.ViewPlacesAndSlots;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewPlacesAndSlotsTest {

    private static HallBase hallBase;
    private static WorkplaceBase workplaceBase;
    private static String date;
    private static ByteArrayInputStream inScanner;
    private Scanner scanner;
    private static ViewPlacesAndSlots viewPlacesAndSlots;
    private static ByteArrayOutputStream outToScreen;

    @BeforeEach
    public void startInit(){
        hallBase = CoworkingContext.getHallBase();
        hallBase.initHallBase();
        workplaceBase = CoworkingContext.getWorkplaceBase();
        workplaceBase.initPlaceBase();
        viewPlacesAndSlots = new ViewPlacesAndSlots();
    }

    @Test
    void viewAllFreeSlotsByEnterDate() throws IOException {
        date = "2025-02-13";
        inScanner = new ByteArrayInputStream(date.getBytes());
        outToScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outToScreen));
        scanner = new Scanner(inScanner);
        viewPlacesAndSlots.viewAllFreeSlotsByEnterDate(scanner);
        outToScreen.flush();
        String allWrittenLines = new String(outToScreen.toByteArray());
        assertTrue(allWrittenLines.contains("На " + date + " все залы и рабочие места свободы! \n" +
                                            "Милости просим!"));
    }

    @Test
    void viewAllPlaces() throws IOException {
        outToScreen = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outToScreen));
        viewPlacesAndSlots.viewAllPlaces();
        outToScreen.flush();
        String allWrittenLines = new String(outToScreen.toByteArray());
        assertTrue(allWrittenLines.contains("Доступные рабочие места:"));
        assertTrue(allWrittenLines.contains("Доступные конференц-залы:"));
        assertTrue(allWrittenLines.contains("Рабочее место № -"));
        assertTrue(allWrittenLines.contains("Зал № -"));
    }
}