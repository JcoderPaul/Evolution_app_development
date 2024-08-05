package me.oldboy.cwapp.core.entity;

import lombok.*;

/**
 * Place entity.
 *
 * Класс, представляющий рабочее место/зал (единицу для резервирования)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    /**
     * Unique place ID.
     *
     * Уникальный идентификатор места.
     */
    private Long placeId;

    /**
     * Place species (Hall/Workplace).
     *
     * Вид места (рабочее место/конференц-зал).
     */
    private Species species;

    /**
     * Place number (1,2,3 ... n).
     *
     * Номер (рабочего места/конференц-зала) 1,2,3 ... n.
     */
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
                ", placeNumber - " + placeNumber +
                ']';
    }
}
