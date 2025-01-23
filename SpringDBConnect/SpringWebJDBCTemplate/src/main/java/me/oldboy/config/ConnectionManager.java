package me.oldboy.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@PropertySource(value = "classpath:application.properties")
@ComponentScan(basePackages = "me.oldboy")
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionManager {

    @Value("${db.username}")
    private String LOGIN_KEY;
    @Value("${db.password}")
    private String PASS_KEY;
    @Value("${db.url}")
    private String BASEURL_KEY;
    @Value("${db.driver}")
    private String DB_DRIVER_KEY;

    /* Конфигурируем bean источника данных для Spring-a */
    @Bean("dataSource")
    public DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(DB_DRIVER_KEY);
        dataSource.setUrl(BASEURL_KEY);
        dataSource.setUsername(LOGIN_KEY);
        dataSource.setPassword(PASS_KEY);

        return dataSource;
    }

    /*
        Мы можем связать источник данных и JdbcTemplate через имя метода как показано ниже,
        а можем перенести данный bean в наш отдельный файл конфигурации AppContextConfig.java
        для разгрузки и связать через @Autowired, как сделано в данный момент.

        @Bean
        public JdbcTemplate jdbcTemplate() {
            return new JdbcTemplate(getDataSource()); // Связываем JdbcTemplate и источник данных
        }
    */
}