package me.oldboy.cwapp.input.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.exceptions.repositorys.UserRepositoryException;
import me.oldboy.cwapp.input.entity.Role;
import me.oldboy.cwapp.input.entity.User;
import me.oldboy.cwapp.input.repository.crud.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final Connection connection;

    /* SQL запросы на - создание, обновление и удаление / SQL queries for - create, update, delete and readAll */

    /**
     * Create (Save) user SQL command
     */
    private static final String CREATE_USER_SQL = """
            INSERT INTO coworking.users (login, user_pass, role)
            VALUES (?, ?, ?);
            """;

    /**
     * Delete user SQL command
     */
    private static final String DELETE_USER_BY_ID_SQL = """
            DELETE FROM coworking.users
            WHERE user_id = ?
            """;

    /**
     * Update user SQL command
     */
    private static final String UPDATE_USER_SQL = """
            UPDATE coworking.users
            SET login = ?,
                user_pass = ?,
                role = ?
            WHERE user_id = ?
            """;

    /**
     * Find (Read) all users SQL command
     */
    private static final String FIND_ALL_USERS_SQL = """
            SELECT user_id,
                   login,
                   user_pass,
                   role
            FROM coworking.users
            """;

    /* SQL запрос на чтение с фильтрацией по ID / SQL read query filtered by ID */

    /**
     * Find (Read) user by ID SQL command
     */
    private static final String FIND_USER_BY_ID_SQL = FIND_ALL_USERS_SQL + """
            WHERE user_id = ?
            """;

    /* SQL запрос на чтение с фильтрацией по логину / SQL read query filtered by login */

    /**
     * Find (Read) user only by login SQL command
     */
    private static final String FIND_USER_BY_LOGIN_SQL =
            FIND_ALL_USERS_SQL + """
            WHERE login = ?
            """;

    /* SQL запрос на чтение с фильтрацией по логину и паролю / SQL read query filtered by login and password */

    /**
     * Find (Read) user by login and password SQL command
     */
    private static final String FIND_USER_BY_LOGIN_AND_PASSWORD_SQL =
            FIND_ALL_USERS_SQL + """
            WHERE login = ? AND user_pass = ?
            """;

    /* Блок работы с сущностью user */
    @Override
    public Optional<User> createUser(User user) {
        /*
        Для нас важно получить сгенерированный базой ID записи, поэтому мы передаем константу,
        которая указывает, что сгенерированные ключи должны быть доступны для извлечения. Акцент
        на данной особенности текущего PreparedStatement сделан именно потому, что в других методах
        нам не понадобится возвращать/получать сгенерированный базой данных ID
        */
        try(PreparedStatement prepareStatement =
                    connection.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {

            prepareStatement.setString(1, user.getLogin());
            prepareStatement.setString(2, user.getPassword());
            prepareStatement.setString(3, user.getRole().name());
            /*
            Выполняет оператор SQL для переданного объекта Statement, который должен быть оператором SQL
            языка манипулирования данными (DML), например: INSERT, UPDATE или DELETE, или оператором SQL,
            который ничего не возвращает, например оператор DDL.
            */
            prepareStatement.executeUpdate();
            ResultSet generatedAutoId = prepareStatement.getGeneratedKeys();
            User userToBase = null;
            if(generatedAutoId.next())
            {
                long id = generatedAutoId.getLong("user_id");
                userToBase = new User(id, user.getLogin(), user.getPassword());
            }
            return Optional.ofNullable(userToBase);
        } catch (SQLException sqlException) {
            throw new UserRepositoryException(sqlException);
        }
    }

    @Override
    public List<User> findAllUsers() {
        try(PreparedStatement prepareStatement = connection.prepareStatement(FIND_ALL_USERS_SQL)) {

            ResultSet resultOfQuery = prepareStatement.executeQuery();
            List<User> findAllUsers = new ArrayList<>();
            while (resultOfQuery.next()){
                findAllUsers.add(buildUser(resultOfQuery));
            }
            return findAllUsers;
        } catch (SQLException sqlException) {
            throw new UserRepositoryException(sqlException);
        }
    }

    @Override
    public Optional<User> findUserById(Long id) {
        try (PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_ID_SQL)) {

            prepareStatement.setLong(1, id);

            ResultSet resultSet = prepareStatement.executeQuery();
            User mayBeUser = null;
            if (resultSet.next()) {
                mayBeUser = buildUser(resultSet);
            }
            return Optional.ofNullable(mayBeUser);
        } catch (SQLException sqlException) {
            throw new UserRepositoryException(sqlException);
        }
    }

    @Override
    public Optional<User> findUserByLogin(String login) {
        try (PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_LOGIN_SQL)) {

            prepareStatement.setString(1, login);

            ResultSet resultSet = prepareStatement.executeQuery();
            User mayBeUser = null;
            if (resultSet.next()) {
                mayBeUser = buildUser(resultSet);
            }
            return Optional.ofNullable(mayBeUser);
        } catch (SQLException sqlException) {
            throw new UserRepositoryException(sqlException);
        }
    }

    @Override
    public Optional<User> findUserByLoginAndPassword(String login, String password) {
        try (PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_LOGIN_AND_PASSWORD_SQL)) {

            prepareStatement.setString(1, login);
            prepareStatement.setString(2, password);

            ResultSet resultSet = prepareStatement.executeQuery();
            User mayBeUser = null;
            if (resultSet.next()) {
                mayBeUser = buildUser(resultSet);
            }
            return Optional.ofNullable(mayBeUser);
        } catch (SQLException sqlException) {
            throw new UserRepositoryException(sqlException);
        }
    }

    @Override
    public boolean updateUser(User user) {
        try(PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_USER_SQL)) {

            prepareStatement.setString(1, user.getLogin());
            prepareStatement.setString(2, user.getPassword());
            prepareStatement.setString(3, user.getRole().name());
            prepareStatement.setLong(4, user.getUserId());

            return prepareStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            throw new UserRepositoryException(sqlException);
        }
    }

    @Override
    public boolean deleteUser(Long userId) {
        try(PreparedStatement prepareStatement = connection.prepareStatement(DELETE_USER_BY_ID_SQL)) {

            prepareStatement.setLong(1, userId);

            return prepareStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            throw new UserRepositoryException(sqlException);
        }
    }

    private User buildUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("user_id"),
                resultSet.getString("login"),
                resultSet.getString("user_pass"),
                Role.valueOf(resultSet.getString("role"))
        );
    }
}