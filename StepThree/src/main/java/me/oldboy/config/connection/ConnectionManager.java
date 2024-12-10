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

    private final static String LOGIN_KEY = "db.username";
    private final static String PASS_KEY = "db.password";
    private final static String BASEURL_KEY = "db.url";

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
                    DriverManager.getConnection(PropertiesUtil.get(BASEURL_KEY),
                                                PropertiesUtil.get(LOGIN_KEY),
                                                PropertiesUtil.get(PASS_KEY));
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
