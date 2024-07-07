package me.oldboy.junit.input.entity;

import me.oldboy.input.entity.Slots;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Slots class.
 */
class SlotsTest {

    private static Slots slots;

    @BeforeEach
    public void initBeforeAllTests() {
        slots = new Slots();
    }

    @AfterEach
    public void cleanDataBaseAfterAlTests() {
        slots = new Slots();
    }

    @Test
    void checkSlotsInStartTest() {
        assertEquals(9, slots.getFreeSlots().size());
        assertEquals(0, slots.getReserveSlots().size());
    }

    @Test
    void moveFreeSlotToReserveTest() {
        assertEquals(9, slots.getFreeSlots().size());
        slots.moveFreeSlotToReserve(10);
        assertEquals(8, slots.getFreeSlots().size());
        assertEquals(1, slots.getReserveSlots().size());
    }

    @Test
    void getFreeSlots() {
        slots.getFreeSlots().remove(10);
        assertEquals(8, slots.getFreeSlots().size());

        slots.getFreeSlots().put(19, "19:00 - 20:00");
        assertEquals(9, slots.getFreeSlots().size());
    }

    @Test
    void getReserveSlots() {
        slots.getReserveSlots().put(10, "10:00 - 11:00");
        assertEquals(1, slots.getReserveSlots().size());

        slots.getReserveSlots().remove(10);
        assertEquals(0, slots.getReserveSlots().size());
    }
}