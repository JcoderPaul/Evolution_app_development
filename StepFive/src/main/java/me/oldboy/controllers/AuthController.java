package me.oldboy.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.jwt_config.JwtTokenGenerator;
import me.oldboy.config.security_details.ClientDetailsService;
import me.oldboy.config.security_details.SecurityUserDetails;
import me.oldboy.dto.jwt.JwtAuthRequest;
import me.oldboy.dto.jwt.JwtAuthResponse;
import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.exception.user_exception.UserControllerException;
import me.oldboy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Class for handling access to the application functionality registration and user authentication.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final ClientDetailsService clientDetailsService;
    @Autowired
    private final JwtTokenGenerator jwtTokenGenerator;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    /**
     * Registration user and save to DB
     *
     * @param userCreateDto for create (save) user data
     * @return created UserDto
     * @throws JsonProcessingException if user with received registration data is already exist
     */
    @PostMapping("/registration")
    public ResponseEntity<?> regUser(@Validated
                                     @RequestBody
                                     UserCreateDto userCreateDto) {
        long createdId;
        /* Проверяем не дублируются ли данные */
        if (userService.findByLogin(userCreateDto.login()).isPresent()) {
            throw new UserControllerException("Пользователь с именем '" + userCreateDto.login() + "' уже существует!");
        } else {
            createdId = userService.create(userCreateDto);
        }

        Optional<UserReadDto> createdUser = userService.findById(createdId);

        if (createdUser.isPresent()) {
            return ResponseEntity.ok().body(createdUser.get());
        } else {
            return ResponseEntity.badRequest().body("{\"message\": \"Check entered data!\"}");
        }
    }

    /**
     * User authentication in the application
     *
     * @param jwtAuthRequest with login and password user request
     * @return with user id, login and JWT Token response
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> loginUser(@Validated
                                                     @RequestBody
                                                     JwtAuthRequest jwtAuthRequest) {
        SecurityUserDetails userDetails =
                (SecurityUserDetails) clientDetailsService.loadUserByUsername(jwtAuthRequest.getLogin());

        if (!passwordEncoder.matches(jwtAuthRequest.getPassword(), userDetails.getUser().getPassword())) {
            throw new UserControllerException("Введен неверный пароль!");
        }

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();

        long userId = userDetails.getUser().getUserId();
        String userLogin = userDetails.getUser().getLogin();
        String jwtToken = jwtTokenGenerator.getToken(userId, userLogin);

        jwtAuthResponse.setId(userId);
        jwtAuthResponse.setLogin(userLogin);
        jwtAuthResponse.setAccessToken(jwtToken);

        return ResponseEntity.ok().body(jwtAuthResponse);
    }
}
