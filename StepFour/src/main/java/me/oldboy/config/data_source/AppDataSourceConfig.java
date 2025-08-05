package me.oldboy.config.data_source;

import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.yaml_read_adapter.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Data source configuration class, defines data source parameters, migration
 * framework settings, settings of JdbcTemplate, EntityManager and TransactionManager
 */
@Slf4j
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
@EnableTransactionManagement
@RequiredArgsConstructor
public class AppDataSourceConfig {

    /* Настройка источника данных */
    @Value("${datasource.url}")
    private String url;
    @Value("${datasource.driver-class-name}")
    private String driver;
    @Value("${datasource.username}")
    private String username;
    @Value("${datasource.password}")
    private String password;

    /* Настройка Liquibase */
    @Value("${liquibase.change_log}")
    private String changeLogFile;
    @Value("${liquibase.default_schema}")
    private String defaultSchema;
    @Value("${liquibase.enabled}")
    private String enabledLiquibaseStart;

    /**
     * Defines the DriverManagerDataSource for DataSource.
     *
     * @return DataSource - represents a connection factory to a physical data source
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    /**
     * Define liquibase settings.
     *
     * @return SpringLiquibase - Spring wrapper for Liquibase
     */
    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changeLogFile);
        liquibase.setShouldRun(Boolean.parseBoolean(enabledLiquibaseStart));
        liquibase.setDefaultSchema(defaultSchema);
        liquibase.setDataSource(dataSource());
        try {
            liquibase.afterPropertiesSet(); // Manually trigger migration
        } catch (Exception e) {
            throw new RuntimeException("Liquibase migration failed", e);
        }

        return liquibase;
    }

    /**
     * Define JdbcTemplate for data access.
     *
     * @return JdbcTemplate - automatically handles the creation and release of JDBC
     * resources like Connection, Statement, and ResultSet, preventing common errors
     * such as forgetting to close connections.
     */
    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    /**
     * Define EntityManagerFactory configuration.
     *
     * @return LocalContainerEntityManagerFactoryBean - responsible for creating and
     * managing a JPA EntityManagerFactory instance within a Spring application context.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
                new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("me.oldboy.models");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

        return entityManagerFactoryBean;
    }

    /**
     * Define TransactionManager configuration.
     *
     * @return JpaTransactionManager - transaction manager for transactional data access.
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;
    }
}