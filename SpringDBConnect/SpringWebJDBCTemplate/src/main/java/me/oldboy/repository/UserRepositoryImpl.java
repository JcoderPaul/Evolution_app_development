package me.oldboy.repository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.entity.User;
import me.oldboy.entity.options.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Repository
public class UserRepositoryImpl implements CrudRepository<Long, User> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String FIND_USER_BY_ID_TEMPLATE = "SELECT * FROM coworking.users WHERE user_id=?";
    private static final String CREATE_USER_SQL = "INSERT INTO coworking.users (login,user_pass,role) VALUES (?,?,?)";
    private static final String FIND_ALL_SQL = "SELECT * FROM coworking.users";
    private static final String DELETE_USER_SQL = "DELETE FROM coworking.users WHERE user_id=?";

    @Override
    public Optional<User> findById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(FIND_USER_BY_ID_TEMPLATE, userRowMapper, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> save(User user) {
        /*
        При добавлении параметров нужно быть очень внимательным, в данном случае мы добавляем переменные типа
        String, а значит и "роль" должна иметь свой текстовый эквивалент иначе хапнем исключение вида:
        Request processing failed: org.springframework.jdbc.BadSqlGrammarException: PreparedStatementCallback;
        bad SQL grammar [INSERT INTO coworking.users (login,user_pass,role) VALUES (?,?,?)]
        */
        int isSave = jdbcTemplate.update(CREATE_USER_SQL, user.getUserName(), user.getPassword(), user.getRole().name());
        if (isSave > 0) {
               return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, userRowMapper);
    }

    @Override
    public boolean delete(Long id) {
        int isDelete = jdbcTemplate.update(DELETE_USER_SQL, id);
        if (isDelete > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void update(User entity) {

    }

    private RowMapper<User> userRowMapper = (row, rowNumber) ->
            User.builder()
                    .userId(row.getLong("user_id"))
                    .userName(row.getString("login"))
                    .role(Role.valueOf(row.getString("role")))
                    .build();
}
