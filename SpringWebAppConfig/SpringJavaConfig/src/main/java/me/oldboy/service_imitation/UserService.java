package me.oldboy.service_imitation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dao_imitation.UserDao;
import me.oldboy.like_entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class UserService {

    @Autowired
    private UserDao userDao;

    public User getUser(Long id) {
        return userDao.findById(id)
                      .orElse(new User(0L, "Have no User (Unexpected ID)!"));
    }
}
