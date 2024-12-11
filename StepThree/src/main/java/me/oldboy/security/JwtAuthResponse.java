package me.oldboy.security;

import lombok.*;
import me.oldboy.core.model.database.entity.options.Role;

/**
 * Class that accumulates data for its transmission from UserServletLogin (from Application)
 * Класс определяющий состояние (ID, логин, роль и токен) возвращаемый сервером приложения на запрос пользователя
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class JwtAuthResponse {

    private Long id;
    private String login;
    private Role role;
    private String accessToken;

}
