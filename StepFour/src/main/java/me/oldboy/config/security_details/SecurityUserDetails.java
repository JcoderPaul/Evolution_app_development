package me.oldboy.config.security_details;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.models.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class define UserDetails for Spring Security
 */
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUserDetails implements UserDetails {

	private User user;

	/**
	 * Get current user authority list
	 *
	 * @return collection of user Authority
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
		return authorities;
	}

    /* Два следующих метода объясняют системе безопасности откуда брать имя/пароль для аутентификации */

	/**
	 * Get current user login or name
	 *
	 * @return user name (login)
	 */
	@Override
	public String getUsername() {
		return user.getLogin();
	}

	/**
	 * Get current user password
	 *
	 * @return user password
	 */
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Get current user object
	 *
	 * @return User entity
	 */
	public User getUser(){
		return this.user;
	}
}