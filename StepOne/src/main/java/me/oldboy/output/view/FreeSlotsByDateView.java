package me.oldboy.output.view;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.Hall;
import me.oldboy.input.entity.Place;
import me.oldboy.input.entity.Slots;
import me.oldboy.input.entity.Workplace;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.WorkplaceBase;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

/**
 * ReadFreeSlotsByDateDto view entity for CLI
 */
public class FreeSlotsByDateView {

    public FreeSlotsByDateView() {
    }

    private final static ReserveBase reserveBase =
            CoworkingContext.getReserveBase();
    private final static HallBase hallBase =
            CoworkingContext.getHallBase();
    private final static WorkplaceBase workplaceBase =
            CoworkingContext.getWorkplaceBase();

    /**
     * String view of all free slots by date.
     *
     * @param date    the date for slots filtering information
     */
    public void viewFreeSlots(LocalDate date){
        Slots freeSlots = new Slots();
        if(reserveBase.getAllReserveSlots().containsKey(date)){
            Map<Place, Slots> freeSlotsList = reserveBase.showAllSeparateSlotsByDate(date);
            System.out.println("На " + date + " свободны: ");
            for (Map.Entry<Place, Slots> entry : freeSlotsList.entrySet()) {
                Place place = entry.getKey();
                System.out.println(place.getSpecies() +" - "+ place.getNumber() + ": ");
                System.out.println("Номер слота и время: " + entry.getValue().getFreeSlots());
            }
            addNoReservePlace(freeSlotsList, freeSlots, date);
        } else {
            System.out.println("На " + date + " все залы и рабочие места свободы! \nМилости просим!");
        }
    }

    /**
     * String view about all free places, slots by date
     *
     * @param freeSlotListFromBase    the map contains all free places, slots by date
     * @param slots  the Slot entity with reservation and free collection
     * @param date  the filtering date
     */
    private void addNoReservePlace(Map<Place, Slots> freeSlotListFromBase, Slots slots, LocalDate date){
        Map<Integer, Hall> notReserveHall = hallBase.getHallBase();
        Map<Integer, Workplace> notReserveWorkplace = workplaceBase.getWorkplaceBase();
        for (Map.Entry<Place, Slots> entry : freeSlotListFromBase.entrySet()) {
            Place place = entry.getKey();
            if (place.getSpecies().equals("Конференц-зал")) {
                notReserveHall.remove(place.getNumber(), place);
            }
            if (place.getSpecies().equals("Рабочее место")) {
                notReserveWorkplace.remove(place.getNumber(), place);
            }
        }
        System.out.println("\nТак же на " + date + " полностью свободны: ");
        for(Map.Entry<Integer, Hall> entry: notReserveHall.entrySet()){
            System.out.println("Конференц-зал №: " + entry.getValue().getNumber() );
            System.out.println("Номер слота и время: " + slots.getFreeSlots() + "\n");
        }
        for(Map.Entry<Integer, Workplace> entry: notReserveWorkplace.entrySet()){
            System.out.println("Рабочее место №: " + entry.getValue().getNumber());
            System.out.println("Номер слота и время: " + slots.getFreeSlots() + "\n");
        }
    }

    public void viewFreeSlotsByDateAndPlace(LocalDate date, Place place){
        if(reserveBase.getAllReserveSlots().containsKey(date)){
            Map<Place, Slots> placeSlotsMap = reserveBase.showAllSeparateSlotsByDate(date);
            System.out.print("На " + date + " в '");
            for (Map.Entry<Place, Slots> entry : placeSlotsMap.entrySet()) {
                Place keyPlace = entry.getKey();
                if(keyPlace.getSpecies().equals(place.getSpecies()) &&
                        keyPlace.getNumber().equals(place.getNumber())) {
                    System.out.println(keyPlace.getSpecies() +
                                       " - " +
                                       keyPlace.getNumber() +
                                       "' доступны (номер слот и время): ");
                    entry.getValue().getFreeSlots()
                            .forEach((key, value) -> System.out.println(key + " - " + value));
                }
            }
        } else {
            System.out.println("На " + date + " все слоты свободны: \n" +
                    "10 - 10:00 - 11:00\n" +
                    "11 - 11:00 - 12:00\n" +
                    "12 - 12:00 - 13:00\n" +
                    "13 - 13:00 - 14:00\n" +
                    "14 - 14:00 - 15:00\n" +
                    "15 - 15:00 - 16:00\n" +
                    "16 - 16:00 - 17:00\n" +
                    "17 - 17:00 - 18:00\n" +
                    "18 - 18:00 - 19:00\n");
        }
    }
}

