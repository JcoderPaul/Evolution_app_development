package me.oldboy.config.liquibase;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import me.oldboy.config.util.PropertiesUtil;

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
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_CREATE_SCHEMA)){
            preparedStatement.execute();

            Database database =
                    DatabaseFactory.getInstance()
                            .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            database.setLiquibaseSchemaName(SCHEMA_NAME);
            CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, CHANGELOG_PATH);

            updateCommand.execute();

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
     * @param rollbackDepth The depth of the rollback, it all depends on the structure changelog files
     */
    public void rollbackDB(Connection connection, Integer rollbackDepth) {
        try {

            Database database =
                    DatabaseFactory.getInstance()
                            .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            database.setLiquibaseSchemaName(SCHEMA_NAME);
            Liquibase liquibase =
                    new Liquibase(CHANGELOG_PATH, new ClassLoaderResourceAccessor(), database);
            /*
            Для текущей реализации комплекта changelog файлов:
            rollbackDepth = 10 - очистка таблиц с их сохранением
            rollbackDepth = 16 - полная очистка базы, удаление таблиц
            Т.е. принцип отката LIFO - в обратную сторону от наката,
            сначала данные от последнего набора до первого, затем
            таблицы от последней к первой и созданию схемы базы см.
            db.changelog-n.n.sql файлы
            */
            liquibase.rollback(rollbackDepth, null);
            System.out.println("Migrations successfully cancelled!");
        } catch (LiquibaseException exception) {
            System.out.println("SQL Exception in migration:" + exception.getMessage());
        }
    }
}