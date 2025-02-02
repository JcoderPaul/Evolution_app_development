package me.oldboy.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.yml_properties_reader.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "me.oldboy.repository")
@RequiredArgsConstructor
public class DataSourceConfig {

    @Value("${datasource.url}")
    private String url;
    @Value("${datasource.driver-class-name}")
    private String driver;
    @Value("${datasource.username}")
    private String username;
    @Value("${datasource.password}")
    private String password;
    @Value("${jpa.properties.hibernate.dialect}")
    private String dialect;
    @Value("${jpa.properties.hibernate.show_sql}")
    private String show_sql;
    @Value("${jpa.properties.hibernate.format_sql}")
    private String format_sql;
    @Value("${jpa.properties.hibernate.hbm2ddl.auto}")
    private String ddl_auto;

    /*
    Как это не странно, но настройки проброшенные в этот метод, из YML файла можно опустить, т.е. данный
    метод с текущей версией Hibernate-a можно вообще не использовать. Будут взяты свойства по умолчанию.
    */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.show_sql", show_sql);
        properties.put("hibernate.format_sql", format_sql);
        properties.put("hibernate.hbm2ddl.auto", ddl_auto);

        return properties;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    /*
    Для создания фабрики сессий мы можем использовать код написанный ниже в комментарии и он, будет работать
    (и приложение выполнит свои функции), однако, если изучить документацию, то нам объясняют почему и зачем
    необходимо использовать класс LocalContainerEntityManagerFactoryBean.

    @Bean
    public LocalSessionFactoryBean entityManagerFactory() {
        LocalSessionFactoryBean entityManagerFactoryBean =
                new LocalSessionFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("me.oldboy.entity");
        entityManagerFactoryBean.setHibernateProperties(hibernateProperties());

        return entityManagerFactoryBean;
    }
    */

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
                new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("me.oldboy.entity");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        /* Если удалить метод hibernateProperties(), то и данную установку можно не применять (приложение будет работать) */
        entityManagerFactoryBean.setJpaProperties(hibernateProperties());
        entityManagerFactoryBean.setEntityManagerFactoryInterface(EntityManagerFactory.class);

        return entityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;
    }
}