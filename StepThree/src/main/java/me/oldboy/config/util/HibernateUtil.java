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

    public static SessionFactory buildSessionFactory() {
        Configuration configuration = buildConfiguration();
        configuration.configure();

        SessionFactory sessionFactory = configuration.buildSessionFactory();

        return sessionFactory;
    }

    public static Configuration buildConfiguration() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Place.class);
        configuration.addAnnotatedClass(Reservation.class);
        configuration.addAnnotatedClass(Slot.class);
        configuration.addAnnotatedClass(Audit.class);

        return configuration;
    }
}
