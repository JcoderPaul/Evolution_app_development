package me.oldboy.models.entity;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.models.entity.options.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the user type with information: user ID, user login (must be unique),
 * password, role (defines access to application resources).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "userReservation")
@EqualsAndHashCode(exclude = "userReservation")
@Builder
@Entity
@Table(name = "users", schema = "coworking")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    /* Связка с сущностью Reservation. Каскадная чистка таблицы all_reservation в случае удаления связного user-a */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reservation> userReservation = new ArrayList<>();
}
