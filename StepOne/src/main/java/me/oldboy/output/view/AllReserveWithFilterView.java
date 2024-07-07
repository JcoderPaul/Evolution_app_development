package me.oldboy.output.view;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.Place;
import me.oldboy.input.entity.ReserveUnit;
import me.oldboy.input.entity.Slots;
import me.oldboy.input.entity.User;
import me.oldboy.input.repository.ReserveBase;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ReadAllReserveWithFilterDto view entity for CLI
 */
public class AllReserveWithFilterView {

    public AllReserveWithFilterView() {
    }

    private final static ReserveBase reserveBase = CoworkingContext.getReserveBase();

    private final Slots slots = new Slots();

    /**
     * String view of all reserve slots by entering date
     *
     * @param date    the view information date
     */
    public void viewAllReserveSlotsByDate(LocalDate date){
        Slots freeSlots = new Slots();
        if(reserveBase.getAllReserveSlots().containsKey(date)){
            Map<Place, Slots> freeSlotsList = reserveBase.showAllSeparateSlotsByDate(date);
            System.out.println("На " + date + " заняты: ");
            for (Map.Entry<Place, Slots> entry : freeSlotsList.entrySet()) {
                Place place = entry.getKey();
                System.out.println(place.getSpecies() +" - "+ place.getNumber() + ": ");
                System.out.println("Номер слота и время: " + entry.getValue().getReserveSlots());
            }
        } else {
            System.out.println("На " + date + " зарезервированных залов и рабочих мест нет! \nМилости просим!");
        }
    }

    /**
     * String view of all reserve slots by entering User
     *
     * @param user    the view of all reservation by User
     */
    public void viewAllReserveByUser(User user){
        if(!user.getUserReservedUnitList().isEmpty()) {
            List<ReserveUnit> allUserReservePlace = user.getUserReservedUnitList()
                    .values()
                    .stream()
                    .toList();
            System.out.println("Пользователь " + user.getLogin() + " имеет следующие брони: ");
            for (ReserveUnit unit : allUserReservePlace) {
                System.out.println(unit.getReserveDate() + " - " +
                        unit.getPlace().getSpecies() + " № " +
                        unit.getPlace().getNumber() + " слот номер " +
                        unit.getReserveSlot() + " время " +
                        slots.getFreeSlots().get(unit.getReserveSlot()));
            }
        } else {
            System.out.println("У пользователя " + user.getLogin() + " нет броней!");
        }
    }

    /**
     * String view of all reserve Halls
     *
     */
    public void viewAllReserveHall(){
        if(!reserveBase.getAllReserveHalls().isEmpty()) {
            System.out.println("Список зарезервированных залов: ");
            for (ReserveUnit unit : reserveBase.getAllReserveUnit()) {
                if (unit.getPlace().getSpecies().equals("Конференц-зал")) {
                    System.out.println(unit.getReserveDate() + ": " +
                            unit.getPlace().getSpecies() + " № " +
                            unit.getPlace().getNumber() + " слот номер: " +
                            unit.getReserveSlot() + " время " +
                            slots.getFreeSlots().get(unit.getReserveSlot()));
                }
            }
        } else {
            System.out.println("На данный момент нет ни одного зарезервированного зала!");
        }
    }

    /**
     * String view of all reserve Workplaces
     *
     */
    public void viewAllReserveWorkplace(){
        if(!reserveBase.getAllReserveWorkplaces().isEmpty()) {
            System.out.println("Список зарезервированных рабочих мест: ");
            for (ReserveUnit unit : reserveBase.getAllReserveUnit()) {
                if (unit.getPlace().getSpecies().equals("Рабочее место")) {
                    System.out.println(unit.getReserveDate() + ": " +
                            unit.getPlace().getSpecies() + " № " +
                            unit.getPlace().getNumber() + " слот номер " +
                            unit.getReserveSlot() + " время " +
                            slots.getFreeSlots().get(unit.getReserveSlot()));
                }
            }
        } else {
            System.out.println("На данный момент нет ни одного зарезервированного рабочего места!");
        }
    }
}
