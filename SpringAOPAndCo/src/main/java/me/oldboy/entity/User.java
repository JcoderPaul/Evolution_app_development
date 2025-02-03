package me.oldboy.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import me.oldboy.entity.options.Role;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "users", schema = "coworking")
@Schema(description = "Полный получаемый набор данных о сущности User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login")
    @Schema(description = "Логин регистрируемого пользователя", example = "CherepahaTartila")
    private String userName;

    @Column(name = "user_pass")
    @Schema(description = "Пароль регистрируемого пользователя")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
}
