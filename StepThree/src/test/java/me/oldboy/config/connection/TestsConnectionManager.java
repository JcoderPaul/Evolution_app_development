package me.oldboy.config.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для соединения с БД
 *
 * Class for database connection.
 */
public class TestsConnectionManager {

    static {
        loadDriver();
    }

    private TestsConnectionManager() {
    }

    public static Connection getTestBaseConnection(String baseUrl, String userLogin, String userPassword) {
        try {
            Connection connection =
                    DriverManager.getConnection(baseUrl, userLogin, userPassword);
            return connection;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Загрузчик драйвера для связи с БД
     *
     * PostgreSQL driver loader.
     */
    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }
}
