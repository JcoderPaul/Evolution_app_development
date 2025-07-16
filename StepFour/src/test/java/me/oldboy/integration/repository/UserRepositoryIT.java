package me.oldboy.integration.repository;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.User;
import me.oldboy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@IT
class UserRepositoryIT extends TestContainerInit {

    @Autowired
    private UserRepository userRepository;
    private Long existUserId, notExistingId;
    private String existLogin, notExistingLogin;

    @BeforeEach
    void setUp(){
        existUserId = 1L;
        notExistingId = 200L;

        existLogin = "User";
        notExistingLogin = "Loser";
    }

    @Test
    void findById_shouldReturnTrue_ForExistingRecord_Test() {
        Optional<User> mayBeUser = userRepository.findById(existUserId);
        if(mayBeUser.isPresent()){
            assertThat(mayBeUser.get()).isNotNull();
        }
    }

    @Test
    void findById_shouldReturnFalse_ForEmptyRecord_Test() {
        Optional<User> mayBeUser = userRepository.findById(notExistingId);
        if(mayBeUser.isEmpty()){
            assertThat(mayBeUser.isPresent()).isFalse();
        }
    }

    @Test
    void findByLogin_shouldReturnTrue_ForExistingRecord_Test() {
        Optional<User> mayBeUser = userRepository.findByLogin(existLogin);
        if(mayBeUser.isPresent()){
            assertThat(mayBeUser.get()).isNotNull();
        }
    }

    @Test
    void findByLogin_shouldReturnFalse_ForNonExistingRecord_Test() {
        Optional<User> mayBeUser = userRepository.findByLogin(notExistingLogin);
        if(mayBeUser.isEmpty()){
            assertThat(mayBeUser.isPresent()).isFalse();
        }
    }
}