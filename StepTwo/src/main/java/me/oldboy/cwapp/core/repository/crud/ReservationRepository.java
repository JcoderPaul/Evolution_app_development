package me.oldboy.cwapp.core.repository.crud;

import me.oldboy.cwapp.core.entity.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    /* CRUD - Create */
    Optional<Reservation> createReservation(Reservation newReservation);
    /* CRUD - Read */
    Optional<Reservation> findReservationById(Long reservationId);
    Optional<List<Reservation>> findAllReservation();
    Optional<List<Reservation>> findReservationByDate(LocalDate reservationDate);
    Optional<List<Reservation>> findReservationByUserId(Long userId);
    Optional<List<Reservation>> findReservationByPlaceId(Long placeId);
    Optional<List<Reservation>> findReservationBySlotId(Long slotId);
    Optional<Reservation> findReservationByDatePlaceAndSlot(LocalDate reservationDate,
                                                            Long placeId,
                                                            Long slotId);
    /* CRUD - Update */
    boolean updateReservation(Reservation reservation);
    /* CRUD - Delete */
    boolean deleteReservation(Long reservationId);
}
