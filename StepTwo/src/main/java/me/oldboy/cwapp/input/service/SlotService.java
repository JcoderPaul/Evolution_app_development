package me.oldboy.cwapp.input.service;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.exceptions.services.SlotServiceException;
import me.oldboy.cwapp.input.entity.Reservation;
import me.oldboy.cwapp.input.entity.Slot;
import me.oldboy.cwapp.input.repository.crud.ReservationRepository;
import me.oldboy.cwapp.input.repository.crud.SlotRepository;

import java.util.*;

/**
 * Slot service layer - getting pure entities.
 */
@RequiredArgsConstructor
public class SlotService {

    private final ReservationRepository reservationRepository;
    private final SlotRepository slotRepository;

    /**
     * Create (save) new slot to base.
     *
     * @param slot the new slot for creating
     * @throws SlotServiceException if create already exist slot
     *
     * @return new created (and save to base) slot ID
     */
    public Long createSlot(Slot slot){
        Optional<Slot> mayBeCreate = slotRepository.createSlot(slot);
        if(mayBeCreate.isEmpty()){
            throw new SlotServiceException("Возможно" + "'" + slot.getSlotNumber() + "' : " +
                                           slot.getTimeStart() + " - " + slot.getTimeFinish() +
                                           " уже существует!");
        } else
            return mayBeCreate.get().getSlotId();
    }

    /**
     * Find all slots.
     *
     * @throws SlotServiceException if slot base empty
     *
     * @return List of all finding slots
     */
    public List<Slot> findAllSlots(){
        List<Slot> mayBeSlotsList = slotRepository.findAllSlots();
        if(mayBeSlotsList.isEmpty()) {
            throw new SlotServiceException("База слотов для бронирования пуста");
        } else
            return mayBeSlotsList;
    }

    /**
     * Find slot by ID.
     *
     * @param slotId slot ID for finding
     * @throws SlotServiceException if slot ID not find
     *
     * @return slot find by ID
     */
    public Slot findSlotById(Long slotId){
        Optional<Slot> mayBeSlot = slotRepository.findSlotById(slotId);
        if(mayBeSlot.isEmpty()){
            throw new SlotServiceException("Слот с ID - " + slotId + " не найден!");
        } else
            return mayBeSlot.get();
    }

    /**
     * Find slot by number.
     *
     * @param slotNumber slot number for finding
     * @throws SlotServiceException if slot with concrete number(not ID) not find
     *
     * @return slot find by number
     */
    public Slot findSlotByNumber(Integer slotNumber){
        Optional<Slot> mayBeSlot = slotRepository.findSlotByNumber(slotNumber);
        if(mayBeSlot.isEmpty()){
            throw new SlotServiceException("Слот с номером - " + slotNumber + " не найден!");
        } else
            return mayBeSlot.get();
    }

    /**
     * Update existing slot.
     *
     * @param slot for update
     * @throws SlotServiceException if nave no slot for update in base
     *
     * @return true if delete is success
     *         false if delete if fail
     */
    public boolean updateSlot(Slot slot){
        if(slotRepository.findSlotById(slot.getSlotId()).isEmpty()){
            throw new SlotServiceException("Слот - '" + slot.getSlotNumber() + "': " +
                                           slot.getTimeStart() + " - " + slot.getTimeFinish() +
                                           " нельзя обновить, т.к. слот не существует!");
        } else
            return slotRepository.updateSlot(slot);
    }

    /**
     * Delete slot.
     *
     * @param slotId slot ID for delete
     * @throws SlotServiceException if try to delete non exist slot
     * @throws SlotServiceException if try to delete reservation slot
     *
     * @return true if delete is success
     *         false if delete fail
     */
    public boolean deleteSlot(Long slotId){
        Optional<List<Reservation>> thisSlotReservation =
                reservationRepository.findReservationBySlotId(slotId);
        if(slotRepository.findSlotById(slotId).isEmpty()){
            throw new SlotServiceException("Удаление несуществующего слота невозможно!");
        } else if(thisSlotReservation.isPresent() && thisSlotReservation.get().size() != 0){
            throw new SlotServiceException("Удаление зарезервированного слота невозможно!");
        } else
            return slotRepository.deleteSlot(slotId);
    }
}
