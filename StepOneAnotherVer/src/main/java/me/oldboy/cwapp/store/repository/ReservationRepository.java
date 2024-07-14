package me.oldboy.cwapp.store.repository;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Long create(Reservation newReservation);
    Reservation update(Reservation reservation);
    Optional<Reservation> findById(Long reservationId);
    boolean delete(Long reservationId);
    Optional<List<Reservation>> findByDate(LocalDate reservationDate);
    Optional<List<Reservation>> findByUserId(Long userId);
    Optional<List<Reservation>> findByPlaceId(Long placeId);
    Optional<List<Reservation>> findByDateAndPlace(LocalDate reservationDate, Long reservationPlaceId);
    List<Reservation> findAll();
    boolean isReservationConflict(Reservation newReservation);
}
