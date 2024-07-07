package me.oldboy.output.cli.items;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.User;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.WorkplaceBase;

import java.util.Scanner;

public class PlaceCrudOperations {

    public PlaceCrudOperations() {
    }

    /**
     * CLI menu for CRUD operation with Workplaces
     *
     * @param scanner    the keyboard scanner
     * @param userEnteredToSystem  the entering user
     */
    public void crudOperationForWorkplaces(User userEnteredToSystem, Scanner scanner) {
        WorkplaceBase workplaceBase = CoworkingContext.getWorkplaceBase();
        System.out.println("Вы можете создавать/изменять/удалять/читать рабочие места.\n" +
                "На данный момент доступны рабочие места: " +
                workplaceBase.stringViewWorkplaceBase() +
                "\n\nВыберите действие:" +
                "\n1 - создать не существующее рабочее место;\n" +
                "2 - удалить не зарезервированное рабочее место;\n" +
                "3 - изменить не зарезервированное рабочее место;\n" +
                "4 - просмотреть выбранное рабочее место;\n" +
                "Введите операцию и номер места через пробел, например (2 6): ");
        String makeWorkplaceOperation = scanner.nextLine().trim();
        String[] parsWorkplaceOperation = makeWorkplaceOperation.split("\s+");
        /* Проводится некая валидация */
        parsWorkplaceOperation = validateEnterCommandLine(scanner, parsWorkplaceOperation);

        switch (parsWorkplaceOperation[0]) {
            case "1": workplaceBase.createWorkPlace(userEnteredToSystem,
                    Integer.parseInt(parsWorkplaceOperation[1]));
                break;
            case "2": workplaceBase.removeWorkPlace(userEnteredToSystem,
                    Integer.parseInt(parsWorkplaceOperation[1]));
                break;
            case "3": System.out.println("Введите новый номер рабочего места на который хотите изменить: ");
                String changeNumberTo = scanner.nextLine().trim();
                workplaceBase.updateWorkPlace(userEnteredToSystem,
                        Integer.parseInt(parsWorkplaceOperation[1]),
                        Integer.parseInt(changeNumberTo));
                break;
            case "4":
                System.out.println(workplaceBase.readWorkPlace(Integer.parseInt(parsWorkplaceOperation[1])));
                break;
            default:
                break;
        }
    }

    /**
     * CLI menu for CRUD operation with Halls
     *
     * @param scanner    the keyboard scanner
     * @param userEnteredToSystem  the entering user
     */
    public void crudOperationForHalls(User userEnteredToSystem, Scanner scanner) {
        HallBase hallBase = CoworkingContext.getHallBase();
        System.out.println("Вы можете создавать/изменять/удалять/читать конференц-зал. \n" +
                "На данный момент доступны залы: " +
                hallBase.stringViewHallBase() +
                "\n\nВыберите действие:" +
                "\n1 - создать не существующий зал;\n" +
                "2 - удалить не зарезервированный зал;\n" +
                "3 - изменить не зарезервированное зал;\n" +
                "4 - просмотреть выбранный зал;\n" +
                "Введите операцию и номер зала через пробел, например (1 3): ");
        String makeHallOperation = scanner.nextLine().trim();
        String[] parsHallOperation = makeHallOperation.split("\s+");
        /* Проводится некая валидация */
        parsHallOperation = validateEnterCommandLine(scanner, parsHallOperation);

        switch (parsHallOperation[0]) {
            case "1": hallBase.createHall(userEnteredToSystem, Integer.parseInt(parsHallOperation[1]));
                break;
            case "2": hallBase.removeHall(userEnteredToSystem, Integer.parseInt(parsHallOperation[1]));
                break;
            case "3": System.out.println("Введите новый номер зала на который хотите изменить: ");
                String changeNumberTo = scanner.nextLine().trim();
                hallBase.updateHall(userEnteredToSystem,
                        Integer.parseInt(parsHallOperation[1]),
                        Integer.parseInt(changeNumberTo));
                break;
            case "4": System.out.println(hallBase.readHall(Integer.parseInt(parsHallOperation[1])));
                break;
            default:
                break;
        }
    }

    /**
     * Universal menu validator for CRUD operation with Workplaces and Halls
     *
     * @param scanner    the keyboard scanner
     * @param parsEnterOperation  the array of entering command and place parameter
     */
    private static String[] validateEnterCommandLine(Scanner scanner, String[] parsEnterOperation) {
        String makeAnOperation;
        boolean haveRightEnter = true;
        do{
            if(!(parsEnterOperation.length == 2) ||
                    !parsEnterOperation[0].matches("\\d+") ||
                    !parsEnterOperation[1].matches("\\d+")){
                System.out.println("\nВы ввели команду не верно, ну, попробуйте еще раз, вы сможете:");
                makeAnOperation = scanner.nextLine().trim();
                parsEnterOperation = makeAnOperation.split("\s+");
            } else {
                haveRightEnter = false;
            }
        } while (haveRightEnter);
        return parsEnterOperation;
    }
}
