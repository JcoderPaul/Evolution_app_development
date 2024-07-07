package me.oldboy.junit.output.view;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.view.FreeSlotsByDateView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for ReadFreeSlotsByDateDto.
 */
class FreeSlotsByDateViewTest {

    private static ReserveBase reserveBase;
    private static HallBase hallBase;
    private static WorkplaceBase workplaceBase;
    private static UserBase userBase;
    private static FreeSlotsByDateView readFreeSlotsByDateDto;

    @BeforeAll
    public static void setUp() {
        CoworkingContext.getInstance();
        hallBase = CoworkingContext.getHallBase();
        workplaceBase = CoworkingContext.getWorkplaceBase();
        userBase = CoworkingContext.getUserBase();
        reserveBase = CoworkingContext.getReserveBase();
        readFreeSlotsByDateDto = new FreeSlotsByDateView();
    }

    @Test
    void viewFreeSlots() throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo));
        readFreeSlotsByDateDto.viewFreeSlots(LocalDate.of(1999,06,06));
        bo.flush();
        String allWrittenLines = new String(bo.toByteArray());
        assertTrue(allWrittenLines.contains("На 1999-06-06 все залы и рабочие места свободы!"));
        assertTrue(allWrittenLines.contains("Милости просим!"));
    }
}