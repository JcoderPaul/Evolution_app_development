package me.oldboy.core.model.database.entity;

import lombok.*;
import me.oldboy.core.model.database.entity.options.CwEntity;
import me.oldboy.core.model.database.entity.options.Role;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "userReservation")
@EqualsAndHashCode(exclude = "userReservation")
@Builder
@Entity
@Table(name = "users", schema = "coworking")
public class User implements CwEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login")
    private String userName;

    @Column(name = "user_pass")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    /* Связка с сущностью Reservation. Каскадная чистка таблицы all_reservation в случае удаления связного user-a */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reservation> userReservation = new ArrayList<>();
}
