package me.oldboy.integration.repository;

import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.User;
import me.oldboy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIT extends ITBaseStarter {

    @Autowired
    private UserRepository userRepository;

    private Long existId, nonExistentId;
    private String existLogin, nonExistentLogin;

    @BeforeEach
    void setUp(){
        existId = 1L;
        nonExistentId = 100L;

        existLogin = "Admin";
        nonExistentLogin = "NotAdmin";
    }

    @Test
    void findById_shouldReturnFoundUser_Test() {
        Optional<User> mayBeUser = userRepository.findById(existId);
        if(mayBeUser.isPresent()){
            assertThat(mayBeUser.get().getUserId()).isEqualTo(existId);
        }
    }

    @Test
    void findById_shouldReturnOptionalEmpty_Test() {
        Optional<User> mayBeUser = userRepository.findById(existId);
        if(mayBeUser.isEmpty()){
            assertThat(mayBeUser.isPresent()).isFalse();
        }
    }

    @Test
    void findByLogin_shouldReturnFoundUser_Test() {
        Optional<User> mayBeUser = userRepository.findByLogin(existLogin);
        if(mayBeUser.isPresent()){
            assertThat(mayBeUser.get().getLogin()).isEqualTo(existLogin);
        }
    }

    @Test
    void findByLogin_shouldReturnOptionalEmpty_Test() {
        Optional<User> mayBeUser = userRepository.findByLogin(nonExistentLogin);
        if(mayBeUser.isEmpty()){
            assertThat(mayBeUser.isPresent()).isFalse();
        }
    }
}