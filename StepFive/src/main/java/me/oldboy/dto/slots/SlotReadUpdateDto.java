package me.oldboy.dto.slots;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.time.LocalTime;

@Builder
public record SlotReadUpdateDto(@NotNull
                                @Positive(message = "SlotId can not be null/negative")
                                Long slotId,
                                @NotNull
                                @PositiveOrZero(message = "Slot number can not be blank/null/negative, it must be greater than or equal to 0")
                                Integer slotNumber,
                                @NotNull(message = "Start time can not be null")
                                @Temporal(TemporalType.TIME)
                                LocalTime timeStart,
                                @NotNull(message = "Finish time can not be null")
                                @Temporal(TemporalType.TIME)
                                LocalTime timeFinish) {
}
