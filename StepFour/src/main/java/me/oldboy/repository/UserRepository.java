package me.oldboy.repository;

import me.oldboy.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>,
                                        CrudRepository<User, Long> {

    public Optional<User> findById(@Param("userId") Long userId);

    public Optional<User> findByLogin(@Param("login") String login);
}
