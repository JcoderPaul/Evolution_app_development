package me.oldboy.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.repository.UserRepositoryImpl;
import me.oldboy.entity.User;
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
                              .id(0L)
                              .name("Have no User (Unexpected ID)!")
                              .build());
    }
}
