package me.oldboy.input.repository;

import me.oldboy.input.entity.Hall;
import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.HallBaseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Hall Base for save existing halls.
 */
public class HallBase {

    private final Map<Integer, Hall> hallBase = new HashMap<>();

    private ReserveBase reserveBase;

    public void initHallBase(){
        hallBase.put(1, new Hall(1));
        hallBase.put(2, new Hall(2));
        hallBase.put(3, new Hall(3));
    }

    /**
     * Create new Hall.
     *
     * @param user    the authorization user with Admin status can create new Hall
     * @param hallNumber  the number (name) of creation Hall
     * @throws HallBaseException if the Hall is already exist or user have no permit
     * @return new created Hall
     */
    public Hall createHall(User user, Integer hallNumber){
        if(!hallBase.containsKey(hallNumber) && user.getStatus().equals("admin")){
            hallBase.put(hallNumber, new Hall(hallNumber));
        } else {
            throw new HallBaseException("Конференц-зала с таким именем уже существует " +
                                        "или у вас недостаточно прав доступа!");
        }
        return hallBase.get(hallNumber);
    }

    /**
     * Remove existing Hall.
     *
     * @param user    the authorization user with Admin status can remove Hall from base
     * @param hallNumber  the number (name) of removing Hall
     * @throws HallBaseException if the Hall exist but reserve it can not be removed
     * @throws HallBaseException if the Hall for remove not exist or user have no permit
     * @return removed Hall
     */
    public Hall removeHall(User user, Integer hallNumber){
        Hall removeHall = null;
        if(hallBase.containsKey(hallNumber) && user.getStatus().equals("admin")){
            if(!reserveBase.getAllReserveHalls().contains(hallNumber)) {
                removeHall = hallBase.remove(hallNumber);
            } else {
                throw new HallBaseException("Данный зал удалить нельзя, он забронирован!");
            }
        } else {
            throw new HallBaseException("Конференц-зала с таким именем не существует " +
                                        "или у вас недостаточно прав доступа!");
        }
        return removeHall;
    }

    /**
     * Update existing Hall.
     *
     * @param user    the authorization user with Admin status can update existing Hall
     * @param oldNumber  the old number (name) of updating Hall
     * @param newNumber  the new number (name) of updating Hall
     * @throws HallBaseException if the Hall exist but reserve it can not be updated
     * @throws HallBaseException if the Hall for update not exist or user have no permit
     * @return updated Hall
     */
    public Hall updateHall(User user, Integer oldNumber, Integer newNumber){
        if(hallBase.containsKey(oldNumber) &&
                user.getStatus().equals("admin") &&
                      !hallBase.containsKey(newNumber)){
            if(!reserveBase.getAllReserveHalls().contains(oldNumber)) {
                hallBase.remove(oldNumber);
                hallBase.put(newNumber, new Hall(newNumber));
            } else {
                throw new HallBaseException("Данный зал пока нельзя обновлять, он забронирован!");
            }
        } else {
             throw new HallBaseException("Конференц-зала с таким именем не существует, " +
                                         "новое название не должно повторяться  " +
                                         "или у вас недостаточно прав доступа!");
        }
        return hallBase.get(newNumber);
    }

    /**
     * Read existing Hall.
     *
     * @param number    the number of reading Hall
     * @throws HallBaseException if the Hall not exist
     * @return reading Hall
     */
    public Hall readHall(Integer number){
        Hall mayBeHall = null;
        if(!hallBase.containsKey(number)) {
            throw new HallBaseException("Конференц-зала с таким номером не существует!");
        } else {
            mayBeHall = hallBase.get(number);
        }
            return mayBeHall;
    }

    /**
     * Get all existing Hall.
     *
     * @return map of all existing Halls
     */
    public Map<Integer, Hall> getHallBase() {
        return hallBase;
    }

    /**
     * Add base of reservation - ReserveBase
     *
     * @param reserveBase the reserve base injection
     */
    public void setReserveBase(ReserveBase reserveBase) {
        this.reserveBase = reserveBase;
    }

    /**
     * Get sting view of all existing Hall for CLI.
     *
     * @return string view of all existing Halls
     */
    public String stringViewHallBase(){
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, Hall> prn: hallBase.entrySet()) {
            stringBuilder.append("\n" + prn.getValue().getSpecies() +
                                 " - " +
                                 prn.getValue().getNumber());
        }
        return stringBuilder.toString();
    }
}