package me.oldboy.junit.input.entity;

import me.oldboy.input.entity.Hall;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Hall class.
 */
class HallTest {

    Integer testNameFor = 1;
    Hall testNewHall = new Hall(testNameFor);

    @Test
    void getSpecies() {
        assertEquals("Конференц-зал", testNewHall.getSpecies());
        assertFalse(testNewHall.getSpecies().equals("Рабочее место"));
    }

    @Test
    void getNumber() {
        assertEquals(testNameFor, testNewHall.getNumber());
        assertFalse(testNewHall.getNumber().equals(4));
    }
}