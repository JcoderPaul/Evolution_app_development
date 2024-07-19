package me.oldboy.cwapp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    private Long placeId;
    private Species species;
    private Integer placeNumber;

    public Place(Species species, Integer placeNumber) {
        this.species = species;
        this.placeNumber = placeNumber;
    }

    @Override
    public String toString() {
        return "Place [" +
                "placeId: " + placeId +
                ", species: " + species +
                ", placeNumber: " + placeNumber +
                ']';
    }
}
