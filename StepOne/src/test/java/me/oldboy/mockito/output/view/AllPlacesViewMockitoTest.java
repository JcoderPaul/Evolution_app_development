package me.oldboy.mockito.output.view;

import me.oldboy.input.entity.Hall;
import me.oldboy.input.entity.Workplace;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.view.AllPlacesView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ReadAllPlacesDto.
 */
class AllPlacesViewMockitoTest {

    @Mock
    private HallBase hallBase;

    @Mock
    private WorkplaceBase workplaceBase;

    @InjectMocks
    private AllPlacesView allPlacesView;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllPlacesTest() throws IOException {
        Map<Integer, Hall> testHallBase = new HashMap<>();
        testHallBase.put(1, new Hall(1));
        testHallBase.put(2, new Hall(2));
        Map<Integer, Workplace> testWorkPlaceBase = new HashMap<>();
        testWorkPlaceBase.put(1, new Workplace(1));
        testWorkPlaceBase.put(2, new Workplace(2));

        ByteArrayOutputStream outPut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outPut));

        when(hallBase.getHallBase()).thenReturn(testHallBase);
        when(workplaceBase.getWorkplaceBase()).thenReturn(testWorkPlaceBase);

        allPlacesView.getAllPlaces();
        outPut.flush();
        String allWrittenLines = new String(outPut.toByteArray());
        assertEquals("Доступные рабочие места: \r\n" +
                "Рабочее место № - " + testWorkPlaceBase.get(1).getNumber() + "\r\n" +
                "Рабочее место № - " + testWorkPlaceBase.get(2).getNumber() + "\r\n" +
                "Доступные конференц-залы: \r\n" +
                "Зал № - " + testHallBase.get(1).getNumber() + "\r\n" +
                "Зал № - " + testHallBase.get(2).getNumber() + "\r\n", allWrittenLines);
    }
}