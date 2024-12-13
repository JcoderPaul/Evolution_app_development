package me.oldboy.config.connection;

import me.oldboy.config.util.PropertiesUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для соединения с БД
 *
 * Class for database connection.
 */
public class ConnectionManager {

    private final static String BASEURL_KEY = "jdbc:postgresql://" +
            System.getenv("POSTGRESQL_CONTAINER_NAME") + ":" +
            System.getenv("DB_CONTAINER_PORT") + "/" +
            System.getenv("POSTGRES_DB");
    private final static String LOGIN_KEY = System.getenv("HIBERNATE_USERNAME");
    private final static String PASS_KEY = System.getenv("HIBERNATE_PASSWORD");

    static {
        loadDriver();
    }

    private ConnectionManager() {
    }

    /**
     * Gets the single instance of ConnectorDB.
     *
     * @return single instance of ConnectorDB
     */
    public static Connection getBaseConnection() {
        try {
            Connection connection =
                    DriverManager.getConnection(BASEURL_KEY,
                                                LOGIN_KEY,
                                                PASS_KEY);
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
