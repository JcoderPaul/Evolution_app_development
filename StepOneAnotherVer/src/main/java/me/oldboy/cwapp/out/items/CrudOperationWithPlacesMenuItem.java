package me.oldboy.cwapp.out.items;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.handlers.PlaceViewHandler;

import java.util.Scanner;

@RequiredArgsConstructor
public class CrudOperationWithPlacesMenuItem {

    private final PlaceViewHandler placeViewHandler;

    public void makeOperation(Scanner scanner, Long userId){
        Boolean leaveMenu = true;
        do{
            System.out.println("Выберите тип операции: " +
                    "\n1 - создание рабочего места/конференц зала;" +
                    "\n2 - удаление рабочего места/конференц зала;" +
                    "\n3 - редактирование рабочего места/конференц зала;" +
                    "\n4 - просмотр существующих рабочих мест и конференц залов; " +
                    "\n5 - выход из текущего меню;\n\n" +
                    "Сделайте выбор и нажмите ввод: ");
            String choiceMenuItem = scanner.nextLine();

            switch (choiceMenuItem) {
                case "1":
                    placeViewHandler.createNewPlace(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "2":
                    placeViewHandler.deletePlace(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "3":
                    placeViewHandler.updatePlace(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "4":
                    placeViewHandler.showAllHallsAndWorkplaces();
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "5":
                    leaveMenu = false;
                    break;
                default:
                    break;
            }
        } while (leaveMenu);
    }
}
