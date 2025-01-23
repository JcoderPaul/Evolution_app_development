package me.oldboy.repository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.base_connect_config.ConnectionManager;
import me.oldboy.entity.User;
import me.oldboy.entity.options.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Repository
public class UserRepositoryImpl implements CrudRepository<Long, User> {

    @Autowired
    private ConnectionManager connectionManager;
    private Connection connection;

    private static final String FIND_USER_BY_ID_SQL = """
            SELECT * FROM coworking.users WHERE user_id = ?
            """;

    private static final String CREATE_USER_SQL = """
            INSERT INTO coworking.users (login, user_pass, role)
            VALUES (?, ?, ?);
            """;
    @Override
    public Optional<User> findById(Long id) {
        User mayBeUser = null;
        connection = connectionManager.getBaseConnection();
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
    public User save(User user) {
        User userToBase = null;
        connection = connectionManager.getBaseConnection();
        try(PreparedStatement prepareStatement =
                    connection.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {

            prepareStatement.setString(1, user.getUserName());
            prepareStatement.setString(2, user.getPassword());
            prepareStatement.setString(3, user.getRole().name());

            prepareStatement.executeUpdate();
            ResultSet generatedAutoId = prepareStatement.getGeneratedKeys();

            if(generatedAutoId.next())
            {
                long id = generatedAutoId.getLong("user_id");
                userToBase = User.builder()
                        .userId(id)
                        .userName(user.getUserName())
                        .role(user.getRole())
                        .build();
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return userToBase;
    }

    private User buildUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("user_id"),
                resultSet.getString("login"),
                resultSet.getString("user_pass"),
                Role.valueOf(resultSet.getString("role"))
        );
    }

    /*
    Методы не реализованы, ввиду однотипности подхода, подробный пример можно найти на:
    https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/src/main/java/me/oldboy/cwapp/core/repository/UserRepositoryImpl.java
    */
    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public void update(User entity) {

    }
}
