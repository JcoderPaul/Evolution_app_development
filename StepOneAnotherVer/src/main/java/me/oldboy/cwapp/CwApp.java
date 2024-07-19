package me.oldboy.cwapp;

import me.oldboy.cwapp.out.MainMenu;

import java.util.Scanner;

public class CwApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MainMenu.getInstance();
        MainMenu.startMainMenu(scanner);
    }
}
