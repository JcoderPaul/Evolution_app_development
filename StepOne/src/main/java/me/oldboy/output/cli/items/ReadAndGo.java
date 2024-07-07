package me.oldboy.output.cli.items;

import java.util.Scanner;

public class ReadAndGo {

    public ReadAndGo() {
    }

    public static void readAndGoForward(Scanner scanner){
        System.out.println("\nЕсли изучили информацию нажмите Enter для продолжения.");
        scanner.nextLine();
    }
}
