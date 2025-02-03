package me.oldboy.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.yml_properties_reader.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
@RequiredArgsConstructor
public class LiquibaseConfig {

    @Autowired
    private DataSource dataSource;

    /* Настройки Liquibase */
    @Value("${liquibase.change-log}")
    private String changeLogFile;
    @Value("${liquibase.liquibase-schema}")
    private String schemaName;
    @Value("${liquibase.enabled}")
    private String liquibaseIsEnable;

    @Bean
    public SpringLiquibase liquibase() {
        createSchema();

        SpringLiquibase liquibase = new SpringLiquibase();
        /*
        Если вдруг мы сами уже создали SCHEMA с неким названием, то можем
        подключить (задать) ее к функционалу Liquibase используя метод:

        liquibase.setLiquibaseSchema(schemaName);
        */
        liquibase.setChangeLog(changeLogFile);
        liquibase.setDataSource(dataSource);

        /* Указываем, где хранить служебные таблицы Liquibase */
        liquibase.setDefaultSchema(schemaName);

        /* Условно "ручное" управление запуском Liquibase */
        if (liquibaseIsEnable.equals("false")) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(true);
            log.info("Configuring Liquibase and Start");
        }

        return liquibase;
    }

    /* Создадим схему для нашей БД */
    private void createSchema(){
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
