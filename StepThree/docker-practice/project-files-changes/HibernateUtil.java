package me.oldboy.config.util;

import lombok.experimental.UtilityClass;
import me.oldboy.core.model.database.audit.Audit;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.Reservation;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@UtilityClass
public class HibernateUtil {

    private final static String BASEURL_KEY = "jdbc:postgresql://" +
            System.getenv("POSTGRESQL_CONTAINER_NAME") + ":" +
            System.getenv("DB_CONTAINER_PORT") + "/" +
            System.getenv("POSTGRES_DB");
    private final static String LOGIN_KEY = System.getenv("HIBERNATE_USERNAME");
    private final static String PASS_KEY = System.getenv("HIBERNATE_PASSWORD");

    public static SessionFactory buildSessionFactory() {
        Configuration configuration = buildConfiguration();
        configuration.configure();

        SessionFactory sessionFactory = configuration.buildSessionFactory();

        return sessionFactory;
    }

    public static Configuration buildConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.url", BASEURL_KEY);
        configuration.setProperty("hibernate.connection.username", LOGIN_KEY);
        configuration.setProperty("hibernate.connection.password", PASS_KEY);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Place.class);
        configuration.addAnnotatedClass(Reservation.class);
        configuration.addAnnotatedClass(Slot.class);
        configuration.addAnnotatedClass(Audit.class);

        return configuration;
    }
}
