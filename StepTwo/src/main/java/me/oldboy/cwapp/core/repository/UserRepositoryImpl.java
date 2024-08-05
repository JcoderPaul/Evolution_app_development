package me.oldboy.cwapp.core.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.core.entity.Role;
import me.oldboy.cwapp.core.entity.User;
import me.oldboy.cwapp.core.repository.crud.UserRepository;

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
        User userToBase = null;
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

            if(generatedAutoId.next())
            {
                long id = generatedAutoId.getLong("user_id");
                userToBase = new User(id, user.getLogin(), user.getPassword());
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(userToBase);
    }

    @Override
    public List<User> findAllUsers() {
        List<User> findAllUsers = new ArrayList<>();
        try(PreparedStatement prepareStatement = connection.prepareStatement(FIND_ALL_USERS_SQL)) {
            ResultSet resultOfQuery = prepareStatement.executeQuery();
            while (resultOfQuery.next()){
                findAllUsers.add(buildUser(resultOfQuery));
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return findAllUsers;
    }

    @Override
    public Optional<User> findUserById(Long id) {
        User mayBeUser = null;
        try (PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_ID_SQL)) {
            prepareStatement.setLong(1, id);
            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                mayBeUser = buildUser(resultSet);
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(mayBeUser);
    }

    @Override
    public Optional<User> findUserByLogin(String login) {
        User mayBeUser = null;
        try (PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_LOGIN_SQL)) {
            prepareStatement.setString(1, login);
            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                mayBeUser = buildUser(resultSet);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(mayBeUser);
    }

    @Override
    public Optional<User> findUserByLoginAndPassword(String login, String password) {
        User mayBeUser = null;
        try (PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_LOGIN_AND_PASSWORD_SQL)) {
            prepareStatement.setString(1, login);
            prepareStatement.setString(2, password);
            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                mayBeUser = buildUser(resultSet);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(mayBeUser);
    }

    @Override
    public boolean updateUser(User user) {
        Boolean isUserUpdated = false;
        try(PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_USER_SQL)) {

            prepareStatement.setString(1, user.getLogin());
            prepareStatement.setString(2, user.getPassword());
            prepareStatement.setString(3, user.getRole().name());
            prepareStatement.setLong(4, user.getUserId());

            isUserUpdated = prepareStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return isUserUpdated;
    }

    @Override
    public boolean deleteUser(Long userId) {
        Boolean isUserDeleted = false;
        try(PreparedStatement prepareStatement = connection.prepareStatement(DELETE_USER_BY_ID_SQL)) {
            prepareStatement.setLong(1, userId);
            isUserDeleted = prepareStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return isUserDeleted;
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