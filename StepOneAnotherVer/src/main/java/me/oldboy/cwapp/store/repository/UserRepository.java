package me.oldboy.cwapp.store.repository;

import me.oldboy.cwapp.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Long create(User user);
    Optional<User> findById(Long userId);
    User update(User user);
    boolean delete(Long userId);
    Optional<User> findUserByLogin(String userLogin);
    List<User> findAll();

}