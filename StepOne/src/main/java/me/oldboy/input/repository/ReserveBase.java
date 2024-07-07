package me.oldboy.input.repository;

import me.oldboy.input.entity.*;
import me.oldboy.input.exeptions.ReserveBaseException;

import java.time.LocalDate;
import java.util.*;

/**
 * Base of all reservation slots.
 */

public class ReserveBase {

    private final UserBase userBase;

    private final Map<LocalDate, Map<Integer, ReserveUnit>> allReserveSlots = new HashMap<>();

    public ReserveBase(UserBase userBase) {
        this.userBase = userBase;
    }

    /**
     * Reserve new slot.
     *
     * @param user    the authorization user can reserve time slot workplace or hall
     * @param reserveUnit  the entity with reservation parameter (place, date, slot)
     * @throws ReserveBaseException if the slot is already reserve
     * @return true if reservation is success, false if reservation is fail
     */
    public boolean reserveSlot(User user, ReserveUnit reserveUnit){
        boolean mayBeReserve = false;
        LocalDate reserveDate = reserveUnit.getReserveDate();
        Integer reserveKeyValue = reserveUnit.hashCode();
        if(!allReserveSlots.containsKey(reserveDate)){
            Map<Integer, ReserveUnit> reserveUnitListForThisDate = new HashMap<>();
            reserveUnitListForThisDate.put(reserveKeyValue, reserveUnit);
            allReserveSlots.put(reserveDate, reserveUnitListForThisDate);
            userBase.getUsersBase()
                    .get(user.getLogin())
                    .getUserReservedUnitList()
                    .put(reserveKeyValue, reserveUnit);
            mayBeReserve = true;
        } else if (!allReserveSlots.get(reserveDate)
                                   .containsKey(reserveKeyValue) ||
                   !allReserveSlots.get(reserveDate)
                                   .get(reserveKeyValue)
                                   .equals(reserveUnit)) {
            allReserveSlots.get(reserveDate).put(reserveKeyValue, reserveUnit);
            userBase.getUsersBase()
                    .get(user.getLogin())
                    .getUserReservedUnitList()
                    .put(reserveKeyValue, reserveUnit);;
            mayBeReserve = true;
        } else {
            throw new ReserveBaseException("Данный слот занят!");
        }
        return mayBeReserve;
    }

    /**
     * Remove reservation slot.
     *
     * @param user    the authorization user can remove own reserve time slot workplace or hall
     * @param removeReserveUnit  the remove entity with reservation parameter (place, date, slot)
     * @throws ReserveBaseException if the slot is already removed reserve or not existed
     * @throws ReserveBaseException if reservation date completely free
     * @return true if remove reservation is success, false if remove reservation is fail
     */
    public boolean removeReserveSlot(User user, ReserveUnit removeReserveUnit){
        boolean mayBeRemoveReserve = false;
        LocalDate keyReserveDate = removeReserveUnit.getReserveDate();
        Integer removeKeyValue = removeReserveUnit.hashCode();
        if(allReserveSlots.containsKey(keyReserveDate)) {
            if (allReserveSlots.get(keyReserveDate)
                               .containsKey(removeKeyValue) &&
                allReserveSlots.get(keyReserveDate)
                               .get(removeKeyValue)
                               .equals(removeReserveUnit)) {
                allReserveSlots.get(keyReserveDate)
                               .remove(removeKeyValue);
                userBase.getUsersBase()
                        .get(user.getLogin())
                        .getUserReservedUnitList()
                        .remove(removeKeyValue);
                mayBeRemoveReserve = true;
            } else {
                throw new ReserveBaseException("Данный слот не найден или был уже свободен!");
            }
        } else {
            throw new ReserveBaseException("На данную дату все места и залы свободны!");
        }
        return mayBeRemoveReserve;
    }

    /**
     * Get all free and reserve slots by date.
     *
     * @param data    the date of reservation
     * @return map of all free and reserve places and slots by date
     */
    public Map<Place, Slots> showAllSeparateSlotsByDate(LocalDate data){
        Map<Integer, ReserveUnit> currentDateReserveSlots = allReserveSlots.get(data);
        Map<Place, Slots> reserveSlotsByPlaces = new HashMap<>();
        for (Map.Entry<Integer, ReserveUnit> entry : currentDateReserveSlots.entrySet()) {
            ReserveUnit currentReserveUnit = entry.getValue();
            Place keyPlace = currentReserveUnit.getPlace();
            Integer freeSlotTransfer = currentReserveUnit.getReserveSlot();
                if (!reserveSlotsByPlaces.containsKey(keyPlace)) {
                    Slots freeSlotsForThisPlace = new Slots();
                    freeSlotsForThisPlace.moveFreeSlotToReserve(freeSlotTransfer);
                    reserveSlotsByPlaces.put(keyPlace, freeSlotsForThisPlace);
                } else {
                    reserveSlotsByPlaces.get(keyPlace).moveFreeSlotToReserve(freeSlotTransfer);
                }
        }
        return reserveSlotsByPlaces;
    }

    /**
     * Get all reserve (slots, places, dates).
     *
     * @return list of all reserve places, slots, dates
     */
    public List<ReserveUnit> getAllReserveUnit(){
        List<ReserveUnit> allReserveUnits = new ArrayList<>();
        if(!allReserveSlots.isEmpty()){
            for(Map.Entry<LocalDate, Map<Integer,ReserveUnit>> reserveUnits : allReserveSlots.entrySet()){
                allReserveUnits.addAll(reserveUnits.getValue().values().stream().toList());
            }
        }
        return allReserveUnits;
    }

    /**
     * Get number of all reserve Halls.
     *
     * @return set of all reserve halls
     */
    public Set<Integer> getAllReserveHalls(){
        Set<Integer> allReserveHallNumber = new HashSet<>();
        for(ReserveUnit unit: getAllReserveUnit()){
            if(unit.getPlace().getSpecies().equals("Конференц-зал")){
                allReserveHallNumber.add(unit.getPlace().getNumber());
            }
        }
        return allReserveHallNumber;
    }

    /**
     * Get all reserve Workplaces numbers
     *
     * @return set of all reserve Workplaces number
     */
    public Set<Integer> getAllReserveWorkplaces(){
        Set<Integer> allReserveWorkplacesNumber = new HashSet<>();
        for(ReserveUnit unit: getAllReserveUnit()){
            if(unit.getPlace().getSpecies().equals("Рабочее место")){
                allReserveWorkplacesNumber.add(unit.getPlace().getNumber());
            }
        }
        return allReserveWorkplacesNumber;
    }

    /**
     * Get all reserve places by dates
     *
     * @return map of all reserve (places, slots, placeNumber) by dates
     */
    public Map<LocalDate, Map<Integer, ReserveUnit>> getAllReserveSlots() {
        return allReserveSlots;
    }
}
