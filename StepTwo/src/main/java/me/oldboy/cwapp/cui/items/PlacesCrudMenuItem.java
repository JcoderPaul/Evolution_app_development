package me.oldboy.cwapp.cui.items;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.core.controllers.PlaceController;

import java.util.Scanner;

@RequiredArgsConstructor
public class PlacesCrudMenuItem {

    private final PlaceController placeController;

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
                    placeController.createNewPlace(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "2":
                    placeController.deletePlace(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "3":
                    placeController.updatePlace(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "4":
                    placeController.showAllPlaces();
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
