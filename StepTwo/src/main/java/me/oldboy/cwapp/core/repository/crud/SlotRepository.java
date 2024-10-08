package me.oldboy.cwapp.core.repository.crud;

import me.oldboy.cwapp.core.entity.Slot;

import java.util.List;
import java.util.Optional;

public interface SlotRepository {
    /* CRUD - Create */
    Optional<Slot> createSlot(Slot slot);
    /* CRUD - Read */
    List<Slot> findAllSlots();
    Optional<Slot> findSlotById(Long placeId);
    Optional<Slot> findSlotByNumber(Integer slotNumber);
    /* CRUD - Update */
    boolean updateSlot(Slot slot);
    /* CRUD - Delete */
    boolean deleteSlot(Long slotId);
}
