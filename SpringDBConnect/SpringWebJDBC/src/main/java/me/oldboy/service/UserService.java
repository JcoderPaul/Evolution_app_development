package me.oldboy.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.entity.User;
import me.oldboy.repository.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class UserService {

    @Autowired
    private UserRepositoryImpl userRepository;

    public User getUser(Long id) {
        return userRepository.findById(id)
                      .orElse(User.builder()
                              .userId(0L)
                              .userName("Have no User (Unexpected ID)!")
                              .build());
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

}
