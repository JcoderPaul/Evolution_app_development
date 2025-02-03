package me.oldboy.base_connect_config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
@PropertySource(value = "classpath:application.properties")
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionManager {

    @Value("${db.username}")
    private String LOGIN_KEY;
    @Value("${db.password}")
    private String PASS_KEY;
    @Value("${db.url}")
    private String BASEURL_KEY;

    static {
        loadDriver();
    }

    public Connection getBaseConnection() {
        try {
            Connection connection =
                    DriverManager.getConnection(BASEURL_KEY, LOGIN_KEY, PASS_KEY);
            return connection;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }
}