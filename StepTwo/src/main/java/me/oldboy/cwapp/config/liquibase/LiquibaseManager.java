package me.oldboy.cwapp.config.liquibase;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import me.oldboy.cwapp.config.connection.PropertiesUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Класс для управления созданием/изменением/удалением таблиц БД
 *
 * Class for running Liquibase - database migrations - database managing.
 */
public class LiquibaseManager {

    private static LiquibaseManager instance;

    private LiquibaseManager() {
    }

    public static LiquibaseManager getInstance() {
        if(instance == null){
            instance = new LiquibaseManager();
        }
        return instance;
    }

    /* Путь к основному файлу миграций */
    private static final String CHANGELOG_PATH = PropertiesUtil.get("liquibase.changeLogFile");
    /* Название схемы куда будут помещены таблицы databasechangelog и databasechangeloglock */
    private static final String SCHEMA_NAME = PropertiesUtil.get("liquibase.schemaName");
    /* SQL команда для создания схемы, где будут храниться файлы Liquibase */
    private static final String SQL_CREATE_SCHEMA =
            "CREATE SCHEMA IF NOT EXISTS " + SCHEMA_NAME;

    /**
     * Метод для создания схемы/таблиц и заполнения данными БД
     *
     * Run database migrations with using Liquibase.
     *
     * @param connection The database connection (соединение с БД).
     */
    public void migrationsStart(Connection connection) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQL_CREATE_SCHEMA)){
            preparedStatement.execute();
            Database database =
                    DatabaseFactory.getInstance()
                            .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setLiquibaseSchemaName(SCHEMA_NAME);
            Liquibase liquibase =
                    new Liquibase(CHANGELOG_PATH, new ClassLoaderResourceAccessor(), database);
            liquibase.update();
            System.out.println("Migration is completed successfully");
        } catch (SQLException | LiquibaseException exception) {
            System.out.println("SQL Exception in migration:" + exception.getMessage());
        }
    }

    /**
     * Метод удаляющий все созданные ранее таблицы и схемы методом *.migrationsStart()
     *
     * Rollback database migrations to delete all tables.
     *
     * @param connection The database connection (связь с БД).
     */
    public void rollbackCreatedTables(Connection connection) {
        try {
            Database database =
                    DatabaseFactory.getInstance()
                            .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setLiquibaseSchemaName(SCHEMA_NAME);
            Liquibase liquibase =
                    new Liquibase(CHANGELOG_PATH, new ClassLoaderResourceAccessor(), database);
            liquibase.rollback(12, null);
            System.out.println("Migrations successfully cancelled!");
        } catch (LiquibaseException exception) {
            System.out.println("SQL Exception in migration:" + exception.getMessage());
        }
    }
}