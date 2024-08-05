package me.oldboy.cwapp.cui;

import me.oldboy.cwapp.config.context.CwAppContext;
import me.oldboy.cwapp.cui.items.PlacesCrudMenuItem;
import me.oldboy.cwapp.cui.items.ReservationMenuItem;
import me.oldboy.cwapp.cui.items.SlotsCrudMenuItem;
import me.oldboy.cwapp.cui.items.UserAuthMenuItem;

import java.sql.Connection;
import java.util.Scanner;

public class MainMenu {
    private static MainMenu instance;
    private static UserAuthMenuItem userAuthMenuItem;
    private static PlacesCrudMenuItem placesCrudMenuItem;
    private static SlotsCrudMenuItem slotsCrudMenuItem;
    private static ReservationMenuItem reservationMenuItem;
    private static CwAppContext cwAppContext;
    private MainMenu() {}

    public static MainMenu getInstance(Connection connection) {
        if(instance == null){
            instance = new MainMenu();
        }
        cwAppContext = CwAppContext.getInstance(connection);
        userAuthMenuItem =
                new UserAuthMenuItem(cwAppContext.getUserController());
        placesCrudMenuItem =
                new PlacesCrudMenuItem(cwAppContext.getPlaceController());
        reservationMenuItem =
                new ReservationMenuItem(cwAppContext.getReserveController(),
                        cwAppContext.getReservationService(),
                        cwAppContext.getPlaceController(),
                        cwAppContext.getPlaceService(),
                        cwAppContext.getSlotService());
        slotsCrudMenuItem = new SlotsCrudMenuItem(cwAppContext.getSlotController());
        return instance;
    }

    public static void startMainMenu(Scanner scanner){
        Boolean repeatMenu = true;
        System.out.println("*** Добро пожаловать в наш коворкинг центр ***\n");
        System.out.println("---------------------------------------------------------------------");

        Long userId = userAuthMenuItem.regAndLogin(scanner);
        if(userId == null){
            repeatMenu = false;
        }

        while (repeatMenu) {
            String menu = "\nВыберите интересующий вас пункт меню: \n" +
                    "1 - резервирование и просмотр рабочих мест и конференц-залов;\n" +
                    "2 - управление рабочими местами и конференц-залами (только для ADMIN); \n" +
                    "3 - управление слотами (только для ADMIN); \n" +
                    "4 - покинуть программу;\n\n" +
                    "Сделайте выбор и нажмите ввод: ";
            System.out.print(menu);

            String command = scanner.nextLine().trim();

            switch (command) {
                case "1":
                    reservationMenuItem.manageByReservationAndViewPlace(scanner, userId);
                    break;
                case "2":
                    placesCrudMenuItem.makeOperation(scanner, userId);
                    break;
                case "3":
                    slotsCrudMenuItem.makeOperation(scanner, userId);
                    break;
                case "4":
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