package me.oldboy.config.security_details;

import lombok.RequiredArgsConstructor;
import me.oldboy.exception.user_exception.LoginNotFoundException;
import me.oldboy.models.entity.User;
import me.oldboy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Class for loads user-specific data.
 */
@Service
@RequiredArgsConstructor
public class ClientDetailsService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    /* Извлекаем из БД клиентов (user-ов) по имени */

    /**
     * Get user by user login or name from DB
     *
     * @param login the username identifying the user whose data is required.
     * @return main user information
     * @throws UsernameNotFoundException if the name or login was not found
     */
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> mayBeUser = userRepository.findByLogin(login);
        if (mayBeUser.isEmpty()) {
            throw new LoginNotFoundException("User : " + login + " not found!");
        }
        return new SecurityUserDetails(mayBeUser.get());
    }
}