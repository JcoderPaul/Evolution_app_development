package me.oldboy.output.cli.items;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.controllers.CoworkingSpaceController;
import me.oldboy.input.entity.Place;
import me.oldboy.input.entity.User;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.view.FreeSlotsByDateView;

import java.time.LocalDate;
import java.util.Scanner;

public class ReserveCrudOperation {

    public ReserveCrudOperation() {
    }

    private static FreeSlotsByDateView freeSlotsByDateView =
            new FreeSlotsByDateView();
    private static HallBase hallBase =
            CoworkingContext.getHallBase();
    private static WorkplaceBase workplaceBase =
            CoworkingContext.getWorkplaceBase();
    private static CoworkingSpaceController coworkingSpaceController =
            CoworkingContext.getCoworkingSpaceController();

    /**
     * Remove slot command
     *
     * @param scanner    the keyboard scanner
     * @param userEnteredToSystem    the entering user
     */
    public void removeReserveFromSlot(Scanner scanner, User userEnteredToSystem) {
        System.out.println("С чего вы хотите снять бронь, введите 'зал' или 'место', без кавычек.\n" +
                           "Далее через пробел номер зала или места.\n");
        Place removePlace = getPlaceForReserveOrRemove(scanner);
        System.out.println("Введите дату в формате (yyyy-mm-dd):");
        /* Валидация не проводится, допускаем, что пользователь крайне дисциплинирован */
        String rmvRsvDate = scanner.nextLine().trim();
        System.out.println("Выберите номер слота для снятия резервирования: \n");
        /* Валидация не проводится, допускаем, что пользователь крайне дисциплинирован */
        String rmvRsvSlot = scanner.nextLine().trim();
        coworkingSpaceController.removeReserveSlot(userEnteredToSystem,
                removePlace,
                LocalDate.parse(rmvRsvDate),
                Integer.parseInt(rmvRsvSlot));
    }

    /**
     * Reserve place command
     *
     * @param scanner    the keyboard scanner
     * @param userEnteredToSystem    the entering user
     */
    public void reservePlaceToEnterDateAndSlot(Scanner scanner, User userEnteredToSystem) {
        System.out.println("Что вы хотите забронировать, введите 'зал' или 'место', без кавычек,\n" +
                           "далее через пробел номер зала или места: \n");
        Place reservePlace = getPlaceForReserveOrRemove(scanner);
        System.out.println("Введите дату в формате (yyyy-mm-dd):");
        /* Проверка валидации не проводится, допускаем, что пользователь крайне дисциплинирован */
        String reserveDate = scanner.nextLine().trim();
        freeSlotsByDateView.viewFreeSlotsByDateAndPlace(LocalDate.parse(reserveDate), reservePlace);
        System.out.println("Выберите номер слота для резервирования из списка: ");
        String reserveSlot = scanner.nextLine().trim();
        coworkingSpaceController.reserveSlot(userEnteredToSystem,
                reservePlace,
                LocalDate.parse(reserveDate),
                Integer.parseInt(reserveSlot));
    }

    private Place getPlaceForReserveOrRemove(Scanner scanner) {
        /* Валидация не проводится, допускаем, что пользователь крайне дисциплинирован */
        String createOrRemoveReserve = scanner.nextLine().trim();
        System.out.println(createOrRemoveReserve);
        String[] enterSpeciesAndNumber = createOrRemoveReserve.split("\s+");
        Place createOrRemovePlace = null;
        if(enterSpeciesAndNumber[0].equals("зал")){
            createOrRemovePlace = hallBase.readHall(Integer.parseInt(enterSpeciesAndNumber[1]));
        } else if(enterSpeciesAndNumber[0].equals("место")){
            createOrRemovePlace = workplaceBase.readWorkPlace(Integer.parseInt(enterSpeciesAndNumber[1]));
        }
        return createOrRemovePlace;
    }
}
