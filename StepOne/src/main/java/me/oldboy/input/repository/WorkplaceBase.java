package me.oldboy.input.repository;

import me.oldboy.input.entity.User;
import me.oldboy.input.entity.Workplace;
import me.oldboy.input.exeptions.WorkplaceBaseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Workplace Base for save existing workplaces.
 */
public class WorkplaceBase {

    private final Map<Integer, Workplace> workplaceBase = new HashMap<>();

    private ReserveBase reserveBase;

    public void initPlaceBase(){
        workplaceBase.put(1, new Workplace(1));
        workplaceBase.put(2, new Workplace(2));
        workplaceBase.put(3, new Workplace(3));
        workplaceBase.put(4, new Workplace(4));
        workplaceBase.put(5, new Workplace(5));
        workplaceBase.put(6, new Workplace(6));
        workplaceBase.put(7, new Workplace(7));
        workplaceBase.put(8, new Workplace(8));
        workplaceBase.put(9, new Workplace(9));
        workplaceBase.put(10, new Workplace(10));
    }

    /**
     * Create new workplace.
     *
     * @param user    the authorization user with Admin status can create new Workplace
     * @param numOfPlace  the number of creation workplace
     * @throws WorkplaceBaseException if the workplace is already exist or user have no permit
     * @return new created workplace
     */
    public Workplace createWorkPlace(User user, Integer numOfPlace){
        if(!workplaceBase.containsKey(numOfPlace) && user.getStatus().equals("admin")){
            workplaceBase.put(numOfPlace, new Workplace(numOfPlace));
        } else {
            throw new WorkplaceBaseException("Рабочее место с таким номером уже существует " +
                                             "или у вас недостаточно прав доступа!");
        }
        return workplaceBase.get(numOfPlace);
    }

    /**
     * Remove existing Workplace.
     *
     * @param user    the authorization user with Admin status can remove Workplace from base
     * @param numOfPlace  the number of removing Workplace
     * @throws WorkplaceBaseException if the workplace exist but reserve it can not be removed
     * @throws WorkplaceBaseException if the workplace for remove not exist or user have no permit
     * @return removed Workplace
     */
    public Workplace removeWorkPlace(User user, Integer numOfPlace){
        Workplace removePlace = null;
        if(workplaceBase.containsKey(numOfPlace) && user.getStatus().equals("admin")){
            if(!reserveBase.getAllReserveWorkplaces().contains(numOfPlace)) {
                removePlace = workplaceBase.remove(numOfPlace);
            } else {
                throw new WorkplaceBaseException("Данное рабочее место удалить нельзя, оно забронировано!");
            }
        } else {
            throw new WorkplaceBaseException("Рабочего места с таким номером не существует " +
                               "или у вас недостаточно прав доступа!");
        }
        return removePlace;
    }

    /**
     * Update existing Workplace.
     *
     * @param user    the authorization user with Admin status can update existing Workplace
     * @param oldNumber  the old number of updating Workplace
     * @param newNumber  the new number (change to) of updating Workplace
     * @throws WorkplaceBaseException if the Workplace exist but reserve it can not be updated
     * @throws WorkplaceBaseException if the Workplace for update not exist or user have no permit
     * @return updated Workplace
     */
    public Workplace updateWorkPlace(User user, Integer oldNumber, Integer newNumber){
        if(workplaceBase.containsKey(oldNumber)
                && user.getStatus().equals("admin")
                        && !workplaceBase.containsKey(newNumber)){
            if(!reserveBase.getAllReserveWorkplaces().contains(oldNumber)) {
                workplaceBase.remove(oldNumber);
                workplaceBase.put(newNumber, new Workplace(newNumber));
            } else {
                throw new WorkplaceBaseException("Данное рабочее место пока нельзя " +
                                   "обновлять, оно забронировано!");
            }
        } else {
            throw new WorkplaceBaseException("Рабочего места с таким номером не существует, " +
                               "новый номер не должен повторяться  " +
                               "или у вас недостаточно прав доступа!");
        }
        return workplaceBase.get(newNumber);
    }

    /**
     * Read existing Workplace.
     *
     * @param number    the number of reading Workplace
     * @throws WorkplaceBaseException if the Workplace not exist
     * @return reading Workplace
     */
    public Workplace readWorkPlace(Integer number){
        Workplace myBeWorkplace = null;
        if(!workplaceBase.containsKey(number)) {
            throw new WorkplaceBaseException("Рабочего места с таким номером не существует!");
        } else {
            myBeWorkplace = workplaceBase.get(number);
        }
            return myBeWorkplace;
    }

    /**
     * Get all existing Workplace.
     *
     * @return map of all existing Workplace
     */
    public Map<Integer, Workplace> getWorkplaceBase() {
        return workplaceBase;
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
     * Get sting view of all existing Workplace for CLI.
     *
     * @return string view of all existing Workplace
     */
    public String stringViewWorkplaceBase(){
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, Workplace> prn: workplaceBase.entrySet()) {
            stringBuilder.append("\n" + prn.getValue().getSpecies() +
                                 " - " +
                                 prn.getValue().getNumber());
        }
        return stringBuilder.toString();
    }
}