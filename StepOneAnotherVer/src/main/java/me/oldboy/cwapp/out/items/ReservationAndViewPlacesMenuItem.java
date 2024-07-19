package me.oldboy.cwapp.out.items;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.handlers.FreeReservationSlotsHandler;
import me.oldboy.cwapp.handlers.PlaceViewHandler;
import me.oldboy.cwapp.handlers.ReservationViewHandler;

import java.util.Scanner;

@RequiredArgsConstructor
public class ReservationAndViewPlacesMenuItem {

    private final ReservationViewHandler reservationViewHandler;
    private final PlaceViewHandler placeViewHandler;
    private final FreeReservationSlotsHandler freeReservationSlotsHandler;

    public void manageByReservationAndViewPlace(Scanner scanner, Long userId){
        Boolean leaveMenu = true;
        do{
            System.out.print("Выберите один из пунктов меню: " +
                    "\n1 - просмотр списка всех доступных рабочих мест и конференц-залов;" +
                    "\n2 - просмотр доступных слотов для бронирования на конкретную дату;" +
                    "\n3 - бронирование рабочего места или конференц-зала на определённое время и дату;" +
                    "\n4 - отмена бронирования;" +
                    "\n5 - просмотр всех бронирований с фильтрацией;" +
                    "\n6 - покинуть меню резервирования;\n\n" +
                    "Сделайте выбор и нажмите ввод: ");
            String choiceMenuItem = scanner.nextLine();

            switch (choiceMenuItem) {
                case "1":
                    placeViewHandler.showAllHallsAndWorkplaces();
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "2":
                    freeReservationSlotsHandler.showAllFreeSlotsByDate(scanner);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "3":
                    reservationMenu(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "4":
                    deleteReservationMenu(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "5":
                    viewReservationWithFilter(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "6":
                    leaveMenu = false;
                    break;
                default:
                    break;
            }
        } while (leaveMenu);
    }

    private void reservationMenu(Scanner scanner, Long userId){
        System.out.print("Выберите, что планируете резервировать: " +
                "\n1 - Конференц зал." +
                "\n2 - Рабочее место.\n\n" +
                "Сделайте выбор и нажмите ввод: ");
        String choiceMenuItem = scanner.nextLine();

        switch (choiceMenuItem) {
            case "1":
                reservationViewHandler.reservationHall(scanner, userId);
                break;
            case "2":
                reservationViewHandler.reservationWorkplace(scanner, userId);
                break;
            default:
                break;
        }
    }

    private void deleteReservationMenu(Scanner scanner, Long userId){
        System.out.print("Выберите, с чего планируете снять бронь: " +
                "\n1 - Конференц зал." +
                "\n2 - Рабочее место.\n\n" +
                "Сделайте выбор и нажмите ввод: ");
        String choiceMenuItem = scanner.nextLine();

        switch (choiceMenuItem) {
            case "1":
                reservationViewHandler.deleteHallReservation(scanner, userId);
                break;
            case "2":
                reservationViewHandler.deleteWorkplaceReservation(scanner, userId);
                break;
            default:
                break;
        }
    }

    private void viewReservationWithFilter(Scanner scanner, Long userId){
        System.out.print("Выберите в каком формате желаете просмотреть текущие брони: " +
                "\n1 - все без фильтрации;" +
                "\n2 - отфильтровать по дате;" +
                "\n3 - только ваши брони;" +
                "\n4 - по выбранному ресурсу;\n\n" +
                "Сделайте выбор и нажмите ввод: ");
        String choiceMenuItem = scanner.nextLine();

        switch (choiceMenuItem) {
            case "1":
                reservationViewHandler.showAllReservation();
                break;
            case "2":
                reservationViewHandler.showAllReservationByDate(scanner);
                break;
            case "3":
                reservationViewHandler.showAllReservationByUserId(userId);
                break;
            case "4":
                reservationViewHandler.showAllReservationByPlaceId(scanner);
                break;
            default:
                break;
        }
    }
}
