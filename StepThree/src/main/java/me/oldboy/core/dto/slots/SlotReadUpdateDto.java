package me.oldboy.core.dto.slots;

import lombok.Builder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalTime;

@Builder
public record SlotReadUpdateDto(@NotNull
                                @Positive(message = "SlotId can not be null/negative")
                                Long slotId,
                                @NotNull
                                @PositiveOrZero(message = "Slot number can not be blank/null/negative, it must be greater than or equal to 0")
                                Integer slotNumber,
                                @NotNull(message = "Start time can not be null")
                                LocalTime timeStart,
                                @NotNull(message = "Start time can not be null")
                                LocalTime timeFinish) {
    @Override
    public String toString() {
        return "SlotReadUpdateDto { " +
                "slotId = " + slotId +
                ", slotNumber = " + slotNumber +
                ", timeStart = " + timeStart +
                ", timeFinish = " + timeFinish +
                '}';
    }
}
