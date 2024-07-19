package me.oldboy.cwapp.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String userLogin;
    private String passWord;
    private Role role;

    public User(String userLogin, String passWord, Role role) {
        this.userLogin = userLogin;
        this.passWord = passWord;
        this.role = role;
    }

    @Override
    public String toString() {
        return "User [" +
                "userId: " + userId +
                ", userLogin: '" + userLogin + '\'' +
                ", passWord: '" + passWord + '\'' +
                ", role: " + role +
                ']';
    }
}
