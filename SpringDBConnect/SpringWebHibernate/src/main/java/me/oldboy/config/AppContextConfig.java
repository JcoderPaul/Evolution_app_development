package me.oldboy.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "me.oldboy")
public class AppContextConfig {

    @Bean
    public SessionFactory getSessionFactory() {
        org.hibernate.cfg.Configuration configuration = HibernateConfig.buildConfiguration();

        return configuration.buildSessionFactory();
    }
}
