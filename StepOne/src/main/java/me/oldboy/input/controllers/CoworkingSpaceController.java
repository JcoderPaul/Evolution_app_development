package me.oldboy.input.controllers;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.ReserveUnit;
import me.oldboy.input.entity.Place;
import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.SpaceControllerException;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;

import java.time.LocalDate;

/**
 * Controller for reserve and remove reserve slots.
 */
public class CoworkingSpaceController {

    private final ReserveBase reserveBase;
    private final UserBase userBase;

    public CoworkingSpaceController(ReserveBase reserveBase, UserBase userBase) {
        this.reserveBase = reserveBase;
        this.userBase = userBase;
    }

    /**
     * New slot reservation.
     *
     * @param user          the user making the reserve one slot
     * @param place         the hall or workplace for reservation
     * @param reserveDate   the date for reservation
     * @param slotNumber    the number of reservation slot
     * @throws SpaceControllerException if the slot is already reserved
     * @throws SpaceControllerException if user have no registration in coworking system
     * @return true if slot was reserved, false if reservation failed
     */
    public boolean reserveSlot(User user, Place place, LocalDate reserveDate, Integer slotNumber){
        /* Полной валидации не проводится, допускаем, что пользователь дисциплинирован */
        boolean mayBeReserve = false;
        ReserveUnit newReserveUnit = new ReserveUnit(reserveDate, place, slotNumber);
        Integer mayBeReserveKey = newReserveUnit.hashCode();
        if(userBase.isUserRegister(user.getLogin())){
            if(!user.getUserReservedUnitList().containsKey(mayBeReserveKey)) {
                mayBeReserve = reserveBase.reserveSlot(user, newReserveUnit);
            } else {
                throw new SpaceControllerException("Такая бронь уже есть!");
            }
        } else {
            throw new SpaceControllerException("Пользователь не зарегистрирован!");
        }
        return mayBeReserve;
    }

    /**
     * Existing slot remove reservation.
     *
     * @param user          the user making the reserve this slot
     * @param place         the hall or workplace witch reservation need remove
     * @param reserveDate   the reservation date
     * @param slotNumber    the number of reservation slot for remove
     * @throws SpaceControllerException if the slot is not exist
     * @throws SpaceControllerException if user have no registration in coworking system
     * @return true if reservation of slot was remove, false if removing of reservation failed
     */
    public boolean removeReserveSlot(User user, Place place, LocalDate reserveDate, Integer slotNumber){
        /* Полной валидации не проводится, допускаем, что пользователь дисциплинирован */
        boolean mayBeRemove = false;
        ReserveUnit removeReserveUnit = new ReserveUnit(reserveDate, place, slotNumber);
        Integer mayBeRemoveKey = removeReserveUnit.hashCode();
        if(userBase.isUserRegister(user.getLogin())){
            if(user.getUserReservedUnitList().containsKey(mayBeRemoveKey)){
                  mayBeRemove = reserveBase.removeReserveSlot(user, removeReserveUnit);
            } else {
                throw new SpaceControllerException("Такой брони не существует или у вас недостаточно прав!");
            }
        } else {
            throw new SpaceControllerException("Пользователь не зарегистрирован!");
        }
        return mayBeRemove;
    }
}
