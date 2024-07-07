package me.oldboy.junit.output.view;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.view.AllPlacesView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for ReadAllPlacesDto.
 */
class AllPlacesViewTest {

    private static HallBase hallBase;
    private static WorkplaceBase workplaceBase;
    private static AllPlacesView readAllPlacesDto;

    @BeforeAll
    public static void setUp() {
        CoworkingContext.getInstance();
        hallBase = CoworkingContext.getHallBase();
        workplaceBase = CoworkingContext.getWorkplaceBase();
        readAllPlacesDto = new AllPlacesView(hallBase, workplaceBase);
    }

    @Test
    void getAllPlaces() throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo));
        readAllPlacesDto.getAllPlaces();
        bo.flush();
        String allWrittenLines = new String(bo.toByteArray());
        assertTrue(allWrittenLines.contains("Доступные рабочие места:"));
        assertTrue(allWrittenLines.contains("Доступные конференц-залы:"));
        assertTrue(allWrittenLines.contains(String.valueOf(hallBase.getHallBase()
                                                                   .get(1)
                                                                   .getNumber())));
        assertTrue(allWrittenLines.contains(String.valueOf(workplaceBase.getWorkplaceBase()
                                                                        .get(1)
                                                                        .getNumber())));
    }
}