package me.oldboy.input.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Slots entity, save collection of reserve and free slots.
 */
public class Slots {
    private Map<Integer, String> freeSlots = new TreeMap<>() {
        {
            put(10, "10:00 - 11:00");
            put(11, "11:00 - 12:00");
            put(12, "12:00 - 13:00");
            put(13, "13:00 - 14:00");
            put(14, "14:00 - 15:00");
            put(15, "15:00 - 16:00");
            put(16, "16:00 - 17:00");
            put(17, "17:00 - 18:00");
            put(18, "18:00 - 19:00");
        }
    };

    private Map<Integer, String> reserveSlots = new TreeMap<>();
    /**
     * Move slot from freeSlots collection to reserveSlots collection
     */
    public void moveFreeSlotToReserve(Integer slotNum) {
        if(freeSlots.containsKey(slotNum)) {
            reserveSlots.put(slotNum, freeSlots.remove(slotNum));
        } else {
            throw new RuntimeException("Такого свободного слота нет или вы пытаетесь " +
                                       "резервировать не существующий слот!");
        }
    }

    /**
     * Get all free slots collection.
     *
     * @return a map of all free slots collection
     */
    public Map<Integer, String> getFreeSlots() {
        return freeSlots;
    }

    /**
     * Get all reserve slots collection.
     *
     * @return a map of all reserve slots collection.
     */
    public Map<Integer, String> getReserveSlots() {
        return reserveSlots;
    }
}
