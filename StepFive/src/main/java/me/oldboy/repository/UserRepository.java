package me.oldboy.repository;

import me.oldboy.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Object (DAO) interface for managing User entities.
 *
 * Extends the generic JpaRepository interface with special operations
 * for working with User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>,
                                        CrudRepository<User, Long> {

    public Optional<User> findById(@Param("userId") Long userId);

    public Optional<User> findByLogin(@Param("login") String login);
}
