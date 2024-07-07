package me.oldboy.output.view;

import me.oldboy.input.entity.Hall;
import me.oldboy.input.entity.Workplace;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.WorkplaceBase;

import java.util.Map;

/**
 * ReadAllPlacesDto view entity for CLI
 */
public class AllPlacesView {

    private final HallBase hallBase;
    private final WorkplaceBase workplaceBase;

    public AllPlacesView(HallBase hallBase, WorkplaceBase workplaceBase) {
        this.hallBase = hallBase;
        this.workplaceBase = workplaceBase;
    }

    /**
     * String view of all existing places (Halls and Workplaces).
     *
     */
    public void getAllPlaces(){
        System.out.println("Доступные рабочие места: ");
        for (Map.Entry<Integer, Workplace> entry: workplaceBase.getWorkplaceBase().entrySet()) {
            System.out.println("Рабочее место № - " + entry.getValue().getNumber());
        }

        System.out.println("Доступные конференц-залы: ");
        for (Map.Entry<Integer, Hall> entry: hallBase.getHallBase().entrySet()) {
            System.out.println("Зал № - " + entry.getValue().getNumber());
        }
    }
}
