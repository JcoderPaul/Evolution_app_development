package me.oldboy.config.security_details;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.exception.user_exception.LoginNotFoundException;
import me.oldboy.models.entity.User;
import me.oldboy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EnableJpaRepositories(basePackages = "me.oldboy.repository")
public class ClientDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	/* Извлекаем из БД клиентов (user-ов) по имени */
	@Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		Optional<User> mayBeUser = userRepository.findByLogin(login);
		if (mayBeUser.isEmpty()) {
			throw new LoginNotFoundException("User : " + login + " not found!");
		}
		return new SecurityUserDetails(mayBeUser.get());
	}
}