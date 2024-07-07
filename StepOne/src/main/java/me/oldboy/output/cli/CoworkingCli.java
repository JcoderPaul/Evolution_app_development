package me.oldboy.output.cli;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.User;
import me.oldboy.output.cli.items.MenuItemsInit;
import me.oldboy.output.cli.items.ReadAndGo;

import java.util.Scanner;

/**
 * CoworkingCli class for user communication.
 */
public class CoworkingCli {
    private static CoworkingCli instance;

    private CoworkingCli(){}

    private static boolean repeatMenu = true;
    private final static ReadAndGo readAndGo = new ReadAndGo();
    public static CoworkingCli getInstance(){
        if(instance == null){
            instance = new CoworkingCli();
            CoworkingContext.getInstance();
        }
        return instance;
    }

    /**
     * CLI main interface
     */
    public static void getInterface(){
        Integer haveHalls = CoworkingContext.getHallBase()
                                            .getHallBase()
                                            .size();
        Integer haveWorkplaces = CoworkingContext.getWorkplaceBase()
                                                 .getWorkplaceBase()
                                                 .size();

        String greeting = "*** Добро пожаловать в наш коворкинг центр ***\n" +
                          "На данный момент к вашим услугам: " +
                          haveHalls +
                          " Конференц-зала и " +
                          haveWorkplaces + " рабочих мест.";

        String invite = "Введите логин (если вы у нас в первый раз, регистрация пройдет автоматически): ";

        System.out.println(greeting);
        Scanner scanner = new Scanner(System.in);
        System.out.println(invite);
        String enterLogin = scanner.nextLine().trim();

        if(!CoworkingContext.getUserBase().getUsersBase().containsKey(enterLogin)) {
            CoworkingContext.getUserController().createUser(enterLogin);
            System.out.println("Пользователь: " +
                               CoworkingContext.getUserBase()
                                               .getUsersBase()
                                               .get(enterLogin)
                                               .getLogin() +
                               " создан.\n" +
                               "Новый пользователь вошел в систему.\n");
        } else {
            System.out.println("Вы успешно вошли в систему.\n");
            CoworkingContext.getUserController().login(enterLogin);
        }

        User userEnteredToSystem = CoworkingContext.getUserBase()
                                                   .getUsersBase()
                                                   .get(enterLogin);
        MenuItemsInit.getInstance();

        mainMenu(scanner, userEnteredToSystem);

        System.out.println("\n*** До новых встреч, мы будем ждать вас! ***");
        scanner.close();
    }

    /**
     * CLI main menu
     *
     * @param scanner    the keyboard scanner
     * @param userEnteredToSystem  the entering user
     */
    private static void mainMenu(Scanner scanner, User userEnteredToSystem) {
        do {
            String menu = "\nВам доступны следующие операции: \n" +
                    "1 - просмотр списка всех доступных рабочих мест и конференц-залов;\n" +
                    "2 - просмотр доступных слотов для бронирования на конкретную дату;\n" +
                    "3 - бронирование рабочего места или конференц-зала на определённое время и дату;\n" +
                    "4 - отмена бронирования;\n" +
                    "5 - просмотр всех бронирований;\n" +
                    "6 - просмотр всех бронирований по дате;\n" +
                    "7 - просмотр всех бронирований по пользователю;\n" +
                    "8 - просмотр всех бронирований по ресурсу;\n\n" +
                    "Администраторы имеют доступ к функционалу:\n" +
                    "9 - управление рабочими местами (CRUD);\n" +
                    "10 - управление конференц-залами (CRUD);\n" +
                    "11 - покинуть систему.\n\n" +
                    "Введите номер команды:";
            System.out.println(menu);

            String command = scanner.nextLine().trim();

            switch (command) {
                case "1":
                    MenuItemsInit.getPlacesAndSlots().viewAllPlaces();
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "2":
                    MenuItemsInit.getPlacesAndSlots().viewAllFreeSlotsByEnterDate(scanner);
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "3":
                    MenuItemsInit.getReserveCrudOperation().reservePlaceToEnterDateAndSlot(scanner, userEnteredToSystem);
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "4":
                    MenuItemsInit.getReserveCrudOperation().removeReserveFromSlot(scanner, userEnteredToSystem);
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "5":
                    MenuItemsInit.getViewReserveByFilter().viewAllReservePlaces();
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "6":
                    MenuItemsInit.getViewReserveByFilter().viewAllReserveByDate(scanner);
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "7":
                    CoworkingContext.getAllReserveWithFilterView().viewAllReserveByUser(userEnteredToSystem);
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "8":
                    MenuItemsInit.getViewReserveByFilter().viewReserveByConcretePlaces(scanner);
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "9":
                    MenuItemsInit.getPlaceCrudOperations().crudOperationForWorkplaces(userEnteredToSystem, scanner);
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "10":
                    MenuItemsInit.getPlaceCrudOperations().crudOperationForHalls(userEnteredToSystem, scanner);
                    readAndGo.readAndGoForward(scanner);
                    break;
                case "11":
                    repeatMenu = MenuItemsInit.getExitMenu().exitMenu(scanner, repeatMenu);
                    break;
                default:
                    break;
            }
        } while (repeatMenu);
    }
}
