package me.oldboy.repository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.base_imitation.LikeBase;
import me.oldboy.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Repository
public class UserRepositoryImpl implements CrudRepository<Long, User> {
    @Autowired
    private LikeBase baseImitation;

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public Optional<User> findById(Long id) {
        Optional<User> mayBeUser = baseImitation.getLikeBase().stream()
                                                              .filter(user -> user.getId().equals(id))
                                                              .findAny();
        return mayBeUser;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public void update(User entity) {

    }

    @Override
    public User save(User entity) {
        return null;
    }
}
