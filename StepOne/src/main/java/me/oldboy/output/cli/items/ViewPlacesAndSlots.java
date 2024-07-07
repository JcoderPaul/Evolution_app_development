package me.oldboy.output.cli.items;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.output.view.AllPlacesView;
import me.oldboy.output.view.FreeSlotsByDateView;

import java.time.LocalDate;
import java.util.Scanner;

public class ViewPlacesAndSlots {

    public ViewPlacesAndSlots() {
    }

    /**
     * String view of all free slots filter by date
     *
     * @param scanner    the keyboard scanner
     */
    public void viewAllFreeSlotsByEnterDate(Scanner scanner) {
        FreeSlotsByDateView freeSlotsByDateView =
                new FreeSlotsByDateView();
        System.out.println("Введите дату в формате (yyyy-mm-dd): ");
        /* Проверка валидации не проводится, допускаем, что пользователь крайне дисциплинирован */
        String enterDate = scanner.nextLine().trim();
        freeSlotsByDateView.viewFreeSlots(LocalDate.parse(enterDate));
    }

    /**
     * String view of all places of our coworking center
     */
    public void viewAllPlaces() {
        AllPlacesView allPlacesView =
                new AllPlacesView(CoworkingContext.getHallBase(),
                                  CoworkingContext.getWorkplaceBase());
        allPlacesView.getAllPlaces();
    }
}
