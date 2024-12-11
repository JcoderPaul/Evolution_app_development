package me.oldboy.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * Class that accumulates data for its transmission to UserLoginServlet
 * Класс определяющий состояние (логин и пароль) входящего в систему пользователя
 */
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthRequest {

    @Getter
    @NotEmpty
    @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
    private String login;

    @Getter
    @NotEmpty
    @Size(min = 3, max = 128, message = "Wrong format (to short/to long)")
    private String password;
}
