package me.oldboy.core.model.database.entity;

import lombok.*;
import me.oldboy.core.model.database.entity.options.CwEntity;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "slotReservation")
@EqualsAndHashCode(exclude = "slotReservation")
@Builder
@Entity
@Table(name = "slots", schema = "coworking")
public class Slot implements CwEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long slotId;

    @Column(name = "slot_number")
    private Integer slotNumber;

    @Column(name = "time_start")
    private LocalTime timeStart;

    @Column(name = "time_finish")
    private LocalTime timeFinish;

    /* Связка с сущностью Reservation. Каскадная чистка таблицы all_reservation в случае удаления связного slot-a */
    @Builder.Default
    @OneToMany(mappedBy = "slot", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reservation> slotReservation = new ArrayList<>();
}
