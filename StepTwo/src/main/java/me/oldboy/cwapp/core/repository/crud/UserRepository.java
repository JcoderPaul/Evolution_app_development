package me.oldboy.cwapp.core.repository.crud;

import me.oldboy.cwapp.core.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    /* CRUD - Create */
    Optional<User> createUser(User user);
    /* CRUD - Read */
    List<User> findAllUsers();
    Optional<User> findUserById(Long userId);
    Optional<User> findUserByLogin(String userLogin);
    Optional<User> findUserByLoginAndPassword(String login, String password);
    /* CRUD - Update */
    boolean updateUser(User user);
    /* CRUD - Delete */
    boolean deleteUser(Long userId);
}