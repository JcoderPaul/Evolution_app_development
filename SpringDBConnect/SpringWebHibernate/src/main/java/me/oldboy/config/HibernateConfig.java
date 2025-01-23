package me.oldboy.config;

import me.oldboy.entity.User;
import org.hibernate.cfg.Configuration;

public class HibernateConfig {

    public static Configuration buildConfiguration() {
        Configuration configuration = new Configuration();
        /* Указываем конфигурационный файл с настройками */
        configuration.configure("hibernate.cfg.xml");
        /* Прописываем наши управляемые Hibernate-ом сущности */
        configuration.addAnnotatedClass(User.class);

        return configuration;
    }
}