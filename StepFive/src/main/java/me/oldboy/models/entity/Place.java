package me.oldboy.models.entity;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.models.entity.options.Species;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "placeReservation")
@EqualsAndHashCode(exclude = "placeReservation")
@Builder
@Entity
@Table(name = "places", schema = "coworking")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "species")
    private Species species;

    @Column(name = "place_number")
    private Integer placeNumber;

    /* Связка с сущностью Reservation. Каскадная чистка таблицы all_reservation в случае удаления связного place-a */
    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reservation> placeReservation = new ArrayList<>();
}
