package me.oldboy.cwapp.cui;

import me.oldboy.cwapp.config.connection.ConnectionManager;
import me.oldboy.cwapp.config.liquibase.LiquibaseManager;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MainMenuTest {

    private Scanner scanner;
    private ByteArrayInputStream inScanner;
    private ByteArrayOutputStream outScreen;
    private static Connection connection;
    private static LiquibaseManager liquibaseManager = LiquibaseManager.getInstance();

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:latest")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    @BeforeAll
    public static void startTestContainer() {
        postgresContainer.start();
        connection = ConnectionManager.getTestBaseConnection(postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword()
        );
        liquibaseManager.migrationsStart(connection);
    }

    @AfterAll
    public static void stopTestContainer(){
        liquibaseManager.rollbackCreatedTables(connection);
        postgresContainer.stop();
    }

    /* Тестовая часть */

    @Test
    void startMainMenuTest() throws IOException {
        String userChoice = "2\nAdmin\n1234\n1\n6\n2\n5\n3\n5\n4";
        inScanner = new ByteArrayInputStream(userChoice.getBytes());
        outScreen = new ByteArrayOutputStream();
        scanner = new Scanner(inScanner);

        System.setOut(new PrintStream(outScreen));

        MainMenu.getInstance(connection);
        MainMenu.startMainMenu(scanner);

        outScreen.flush();
        String allWrittenLines = new String(outScreen.toByteArray());
        assertThat(allWrittenLines.contains("\nВыберите интересующий вас пункт меню: \n" +
                "1 - резервирование и просмотр рабочих мест и конференц-залов;\n" +
                "2 - управление рабочими местами и конференц-залами (только для ADMIN); \n" +
                "3 - управление слотами (только для ADMIN); \n" +
                "4 - покинуть программу;\n\n" +
                "Сделайте выбор и нажмите ввод: ")).isTrue();
    }
}