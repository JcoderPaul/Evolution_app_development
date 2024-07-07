package me.oldboy.input.entity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * ReserveUnit entity, save parameters of reservation.
 */
public class ReserveUnit {

    private LocalDate reserveDate;
    private Place place;

    private Integer reserveSlot;

    public ReserveUnit(LocalDate reserveDate, Place place, Integer reserveSlot) {
        this.reserveDate = reserveDate;
        this.place = place;
        this.reserveSlot = reserveSlot;
    }

    /**
     * Get reservation date
     *
     * @return date of reservation
     */
    public LocalDate getReserveDate() {
        return reserveDate;
    }

    /**
     * Get reservation place
     *
     * @return place of reservation
     */
    public Place getPlace() {
        return place;
    }


    /**
     * Get reservation slot
     *
     * @return slot number of reservation
     */
    public Integer getReserveSlot() {
        return reserveSlot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReserveUnit that = (ReserveUnit) o;
        return Objects.equals(reserveDate, that.reserveDate)
                && Objects.equals(place, that.place)
                && Objects.equals(reserveSlot, that.reserveSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reserveDate, place, reserveSlot);
    }
}
