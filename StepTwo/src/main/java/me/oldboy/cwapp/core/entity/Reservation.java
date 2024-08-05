package me.oldboy.cwapp.core.entity;

import lombok.*;

import java.time.LocalDate;

/**
 * Reserve entity, save parameters of reservation.
 *
 * Класс, представляющий единицу бронирования
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    private Long reserveId;
    private LocalDate reserveDate;
    private User user;
    private Place place;
    private Slot slot;

    public Reservation(LocalDate reserveDate, User user, Place place, Slot slot) {
        this.reserveDate = reserveDate;
        this.user = user;
        this.place = place;
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "Reserve [" +
                "reserveId: " + reserveId +
                ", reserveDate: " + reserveDate +
                ", place: " + place.getSpecies() +
                " " + place.getPlaceNumber() +
                ", slot: " + slot.getSlotNumber() +
                " : " + slot.getTimeStart() +
                " - " + slot.getTimeFinish() +
                ']';
    }
}