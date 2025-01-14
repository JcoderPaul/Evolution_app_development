package me.oldboy.dao_imitation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.base_imitation.LikeBase;
import me.oldboy.like_entity.User;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
public class UserDao implements CrudDao<Long, User>{
    private LikeBase baseImitation;

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public Optional<User> findById(Long id) {
        Optional<User> mayBeUser = baseImitation.getLikeBase().stream()
                                                              .filter(user -> user.id().equals(id))
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
