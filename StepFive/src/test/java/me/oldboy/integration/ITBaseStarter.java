package me.oldboy.integration;

import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@IT
public abstract class ITBaseStarter {
    /* Инициализируем тест контейнер и создадим во временной БД нужную нам схему */
    public static final PostgreSQLContainer<?> container =
            new PostgreSQLContainer<>("postgres:13")
                    .withInitScript("db/changelog/db.changelog-0.5.sql");

    @BeforeAll
    static void runContainer() {
        container.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> container.getJdbcUrl());
    }
}
