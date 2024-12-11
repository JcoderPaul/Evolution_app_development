package me.oldboy.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.oldboy.core.model.database.entity.options.Role;

/**
 *Class that defines the user state when the application is running (authenticated = true / false)
 * Класс определяющий состояние пользователя при обращению к приложению (аутентифицирован да / нет)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthUser {

    private String login;
    private Role role;
    private boolean isAuth;

}
