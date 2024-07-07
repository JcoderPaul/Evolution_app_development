package me.oldboy.output.cli.items;

import me.oldboy.input.context.CoworkingContext;

import java.time.LocalDate;
import java.util.Scanner;

public final class ViewReserveByFilter {

    public ViewReserveByFilter() {
    }

    /**
     * String view of reserve Workplaces or Halls
     *
     * @param scanner    the keyboard scanner
     */
    public void viewReserveByConcretePlaces(Scanner scanner) {
        System.out.println("Бронь по каким ресурсам хотите изучить, введите " +
                "'зал' - если \"Конференц-залы\", и 'место' - если \"Рабочие места\": ");
        /* Валидация не проводится, допускаем, что пользователь крайне дисциплинирован */
        String getMeReserveByPlace = scanner.nextLine().trim();
        if(getMeReserveByPlace.equals("зал")){
            CoworkingContext.getAllReserveWithFilterView().viewAllReserveHall();
        } else if(getMeReserveByPlace.equals("место")){
            CoworkingContext.getAllReserveWithFilterView().viewAllReserveWorkplace();
        }
    }

    /**
     * String view all reservation Workplaces and Halls by fix date
     *
     * @param scanner    the keyboard scanner
     */
    public void viewAllReserveByDate(Scanner scanner) {
        System.out.println("На какую дату хотите посмотреть все забронированные " +
                "места, формат (yyyy-mm-dd): ");
        /* Валидация не проводится, допускаем, что пользователь крайне дисциплинирован */
        String viewAllResByDate = scanner.nextLine().trim();
        CoworkingContext.getAllReserveWithFilterView()
                        .viewAllReserveSlotsByDate(LocalDate.parse(viewAllResByDate));
    }

    /**
     * String view all reservation Workplaces and Halls without filter
     */
    public void viewAllReservePlaces() {
        CoworkingContext.getAllReserveWithFilterView().viewAllReserveHall();
        CoworkingContext.getAllReserveWithFilterView().viewAllReserveWorkplace();
    }

}
