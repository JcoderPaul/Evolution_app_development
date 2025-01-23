package me.oldboy.entity;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.entity.options.Role;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {

    private Long userId;
    private String userName;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
}
