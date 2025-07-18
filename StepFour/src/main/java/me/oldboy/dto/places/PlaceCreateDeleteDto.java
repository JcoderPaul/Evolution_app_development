package me.oldboy.dto.places;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import me.oldboy.models.entity.options.Species;

@Builder
public record PlaceCreateDeleteDto(@NotNull(message = "Species can not be blank/empty")
                                   Species species,
                                   @NotNull
                                   @Positive(message = "PlaceNumber can not be null/negative")
                                   Integer placeNumber){
}
