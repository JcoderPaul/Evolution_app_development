package me.oldboy.cwapp;

import me.oldboy.cwapp.config.connection.ConnectionManager;
import me.oldboy.cwapp.config.liquibase.LiquibaseManager;
import me.oldboy.cwapp.cui.MainMenu;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class CwMainApp {
    public static void main(String[] args) {
        try(Connection connection = ConnectionManager.getBaseConnection();
            Scanner scanner = new Scanner(System.in)) {
            LiquibaseManager.getInstance().migrationsStart(connection);
            MainMenu.getInstance(connection);
            MainMenu.startMainMenu(scanner);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
