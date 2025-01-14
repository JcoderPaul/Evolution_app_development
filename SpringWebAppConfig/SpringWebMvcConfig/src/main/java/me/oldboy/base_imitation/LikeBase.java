package me.oldboy.base_imitation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class LikeBase {
    private List<User> likeBase = List.of(new User(1L,"John"),
                                          new User(2L,"Will"),
                                          new User(3L,"Sally"));

    public List<User> getLikeBase() {
        return likeBase;
    }
}
