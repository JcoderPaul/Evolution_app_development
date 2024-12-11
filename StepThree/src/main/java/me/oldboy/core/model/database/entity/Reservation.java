package me.oldboy.core.model.database.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "all_reserves", schema = "coworking")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserve_id")
    private Long reservationId;

    @Column(name = "reserve_date")
    private LocalDate reservationDate;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="user_id", nullable=false) // Ссылка на связанную сущность
    private User user;

    @ManyToOne(targetEntity = Place.class)
    @JoinColumn(name="place_id", nullable=false) // Ссылка на связанную сущность
    private Place place;

    @ManyToOne(targetEntity = Slot.class)
    @JoinColumn(name="slot_id", nullable=false) // Ссылка на связанную сущность
    private Slot slot;
}