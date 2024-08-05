package me.oldboy.cwapp.core.entity;

import lombok.*;

import java.time.LocalTime;

/**
 * Slot entity, save collection of reserve and free slots.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Slot {
    private Long slotId;
    private Integer slotNumber;
    private LocalTime timeStart;
    private LocalTime timeFinish;

    public Slot(Integer slotNumber, LocalTime timeStart, LocalTime timeFinish) {
        this.slotNumber = slotNumber;
        this.timeStart = timeStart;
        this.timeFinish = timeFinish;
    }

    @Override
    public String toString() {
        return " Slot [" +
                " slotId: " + slotId +
                ", slotNumber: " + slotNumber +
                ", time range: " + timeStart +
                " - " + timeFinish + " " +
                ']';
    }
}
