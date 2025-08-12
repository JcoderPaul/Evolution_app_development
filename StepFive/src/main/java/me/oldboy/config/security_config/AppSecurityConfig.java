package me.oldboy.config.security_config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.jwt_config.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Class define security configuration of application
 */
@Slf4j
@Configuration
@EnableWebSecurity(debug = true) // Not for prod
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@ComponentScan("me.oldboy")
@AllArgsConstructor
@NoArgsConstructor
public class AppSecurityConfig {

	@Autowired
	private JwtAuthFilter jwtAuthFilter;

	/**
	 * Defines a filter chain which is capable of being matched against an
	 * HttpServletRequest, in order to decide whether it applies to that
	 * request.
	 *
	 * @param httpSecurity allows configuring web based security for specific http requests
	 * @return main security filter chain
	 */
	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		httpSecurity
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(urlConfig -> urlConfig
						.requestMatchers("/api/registration",
								"/api/login",
								"/v3/api-docs/**",
								"/swagger-ui/**").permitAll()
						.requestMatchers("/api/places/**",
								"/api/slots/**",
								"/api/reservations/**").authenticated()
						.requestMatchers("/api/admin/**").hasAuthority("ADMIN")
						.anyRequest().authenticated());

		return httpSecurity.build();
	}

	/**
	 *	Define password encoder
	 *
	 * @return current password encoder
	 */
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}