package me.oldboy.cwapp.cui.items;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.core.controllers.SlotController;

import java.util.Scanner;

@RequiredArgsConstructor
public class SlotsCrudMenuItem {

    private final SlotController slotController;

    public void makeOperation(Scanner scanner, Long userId){
        Boolean leaveMenu = true;
        do{
            System.out.println("Выберите тип операции: " +
                    "\n1 - создание слота;" +
                    "\n2 - удаление слота;" +
                    "\n3 - редактирование слота;" +
                    "\n4 - просмотр существующих слотов; " +
                    "\n5 - выход из текущего меню;\n\n" +
                    "Сделайте выбор и нажмите ввод: ");
            String choiceMenuItem = scanner.nextLine();

            switch (choiceMenuItem) {
                case "1":
                    slotController.createNewSlot(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "2":
                    slotController.deleteSlot(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "3":
                    slotController.updateSlot(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "4":
                    slotController.viewAllSlots();
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