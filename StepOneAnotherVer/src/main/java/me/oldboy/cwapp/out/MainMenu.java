package me.oldboy.cwapp.out;

import me.oldboy.cwapp.context.CwAppContext;
import me.oldboy.cwapp.out.items.CrudOperationWithPlacesMenuItem;
import me.oldboy.cwapp.out.items.RegLoginMenuItem;
import me.oldboy.cwapp.out.items.ReservationAndViewPlacesMenuItem;

import java.util.Scanner;

public class MainMenu {

    private static MainMenu instance;
    private static RegLoginMenuItem regLoginMenuItem;
    private static CrudOperationWithPlacesMenuItem crudOperationWithPlacesMenuItem;
    private static ReservationAndViewPlacesMenuItem reservationAndViewPlacesMenuItem;
    private static Scanner scanner;
    private MainMenu() {}

    public static MainMenu getInstance() {
        if(instance == null){
            instance = new MainMenu();
        }
        regLoginMenuItem =
                new RegLoginMenuItem(CwAppContext.getInstance().getUserAuthenticationHandler());
        crudOperationWithPlacesMenuItem =
                new CrudOperationWithPlacesMenuItem(CwAppContext.getInstance().getPlaceViewHandler());
        reservationAndViewPlacesMenuItem =
                new ReservationAndViewPlacesMenuItem(CwAppContext.getInstance().getReservationViewHandler(),
                                                     CwAppContext.getInstance().getPlaceViewHandler(),
                                                     CwAppContext.getInstance().getFreeReservationSlotsHandler());
        return instance;
    }

    public static void startMainMenu(Scanner scanner){
        Boolean repeatMenu = true;
        System.out.println("*** Добро пожаловать в наш коворкинг центр ***\n");
        System.out.println("---------------------------------------------------------------------");

        Long userId = regLoginMenuItem.regAndLogin(scanner);
        if(userId == null){
            repeatMenu = false;
        }

        while (repeatMenu) {
            String menu = "\nВыберите интересующий вас пункт меню: \n" +
                    "1 - резервирование и просмотр рабочих мест и конференц-залов;\n" +
                    "2 - управление рабочими местами и конференц-залами (только для ADMIN); \n" +
                    "3 - покинуть программу;\n\n" +
                    "Сделайте выбор и нажмите ввод: ";
            System.out.print(menu);

            String command = scanner.nextLine().trim();

            switch (command) {
                case "1":
                    reservationAndViewPlacesMenuItem.manageByReservationAndViewPlace(scanner, userId);
                    break;
                case "2":
                    crudOperationWithPlacesMenuItem.makeOperation(scanner, userId);
                    break;
                case "3":
                    repeatMenu = false;
                    break;
                default:
                    break;
            }
        }
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Всего хорошего, ждем вас снова!");
        scanner.close();
    }
}