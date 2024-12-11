package me.oldboy.core.dto.places;

import lombok.Builder;
import me.oldboy.core.model.database.entity.options.Species;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Builder
public record PlaceReadUpdateDto(@NotNull
                                 @Positive(message = "PlaceId can not be null/negative")
                                 Long placeId,
                                 @NotNull(message = "Species can not be blank/empty")
                                 Species species,
                                 @NotNull
                                 @Positive(message = "PlaceNumber can not be null/negative")
                                 Integer placeNumber) {
    @Override
    public String toString() {
        return "PlaceReadUpdateDto {" +
                "placeId = " + placeId +
                ", species = " + species +
                ", placeNumber = " + placeNumber +
                '}';
    }
}
