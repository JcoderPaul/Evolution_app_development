package me.oldboy.core.dto.slots;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalTime;

public record SlotCreateDeleteDto(@NotNull
                                  @PositiveOrZero(message = "Slot number can not be blank/null/negative, it must be greater than or equal to 0")
                                  Integer slotNumber,
                                  @NotNull(message = "Start time can not be null")
                                  LocalTime timeStart,
                                  @NotNull(message = "Finish time can not be null")
                                  LocalTime timeFinish) {
    @Override
    public String toString() {
        return "SlotCreateDeleteDto { " +
                "slotNumber = " + slotNumber +
                ", timeStart = " + timeStart +
                ", timeFinish = " + timeFinish +
                '}';
    }
}
