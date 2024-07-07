package me.oldboy.output.cli.items;

import java.util.Scanner;

public class ExitMenu {

    public ExitMenu() {
    }

    /**
     * Exit menu
     */
    public boolean exitMenu(Scanner scanner, Boolean needToRepeat){
        do{
            System.out.print("\nПродолжить работу (Yes/No): ");
            String yesOrNoAnswer = scanner.nextLine().trim();

            if (yesOrNoAnswer.matches("Yes|yes")) {
                needToRepeat = true;
                break;
            }
            else if (yesOrNoAnswer.matches("No|no")) {
                needToRepeat = false;
                break;
            }
            else {
                System.out.println("Неверные символы, будьте внимательны!");
            }
        } while (needToRepeat);

        return needToRepeat;
    }
}