package me.oldboy.cwapp.core.entity;

import lombok.*;

/**
 * User entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String login;
    private String password;
    private Role role;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.role = Role.USER;
    }

    public User(Long id, String login, String password) {
        this.userId = id;
        this.login = login;
        this.password = password;
        this.role = Role.USER;
    }

    @Override
    public String toString() {
        return "User [" +
                "userId - " + userId +
                ", login: '" + login + '\'' +
                ", password: '" + password + '\'' +
                ", role: " + role +
                ']';
    }
}
