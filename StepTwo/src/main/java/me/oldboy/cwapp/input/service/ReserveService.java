package me.oldboy.cwapp.input.service;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.exceptions.services.ReserveServiceException;
import me.oldboy.cwapp.input.entity.Place;
import me.oldboy.cwapp.input.entity.Reservation;
import me.oldboy.cwapp.input.entity.Slot;
import me.oldboy.cwapp.input.entity.User;
import me.oldboy.cwapp.input.repository.crud.PlaceRepository;
import me.oldboy.cwapp.input.repository.crud.ReservationRepository;
import me.oldboy.cwapp.input.repository.crud.SlotRepository;
import me.oldboy.cwapp.input.repository.crud.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReserveService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final PlaceRepository placeRepository;

    /**
     * Create new reservation.
     *
     * @param reservation reservation to create with concrete parameters
     *
     * @return create reservation ID
     */
    public Long createReservation(Reservation reservation) {
        Optional<Reservation> mayBeCreateReservationId = Optional.empty();
        if(isReservationCorrect(reservation)) {
            mayBeCreateReservationId = reservationRepository.createReservation(reservation);
        }
        return mayBeCreateReservationId.get().getReserveId();
    }

    /**
     * Find reservation by ID.
     *
     * @param reservationId reservation ID to find
     * @throws ReserveServiceException if try duplicate reservation
     *
     * @return found reservation by ID
     */
    public Reservation findReservationById(Long reservationId) {
        Optional<Reservation> mayBeReservation = reservationRepository.findReservationById(reservationId);
        if(mayBeReservation.isEmpty()) {
            throw new ReserveServiceException("Бронь с ID - " + reservationId + " не найдена!");
        } else
            return mayBeReservation.get();
    }

    /**
     * Find all reservation.
     *
     * @throws ReserveServiceException if reservation base is empty
     *
     * @return list of all reservation
     */
    public List<Reservation> findAllReservation() {
        Optional<List<Reservation>> mayBeAllReservation = reservationRepository.findAllReservation();
        if(mayBeAllReservation.isEmpty() || mayBeAllReservation.get().size() == 0){
            throw new ReserveServiceException("База броней пуста!");
        } else
            return mayBeAllReservation.get();
    }

    /**
     * Find reservations by date.
     *
     * @param date reservation date for
     * @throws ReserveServiceException if reservation by concrete date is empty
     *
     * @return list of concrete date reservation
     */
    public List<Reservation> findReservationByDate(LocalDate date) {
        Optional<List<Reservation>> mayBeListReservationByDate =
                reservationRepository.findReservationByDate(date);
        if(mayBeListReservationByDate.isEmpty() || mayBeListReservationByDate.get().size() == 0){
            throw new ReserveServiceException("Бронирований на " + date + " не найдено!");
        }
        return mayBeListReservationByDate.get();
    }

    /**
     * Find reservations by place ID.
     *
     * @param placeId place ID to find reservation
     * @throws ReserveServiceException if concrete place ID not found
     * @throws ReserveServiceException if reservation by concrete place ID is empty
     *
     * @return list of reservation by concrete place ID
     */
    public List<Reservation> findReservationByPlaceId(Long placeId){
        Optional<List<Reservation>> mayBeReservation =
                reservationRepository.findReservationByPlaceId(placeId);
        Optional<Place> mayBePlace = placeRepository.findPlaceById(placeId);
        if(mayBePlace.isEmpty()) {
            throw new ReserveServiceException("Место / зал с ID: " + placeId + " не найден(о)!");
        } else if(mayBeReservation.isEmpty() || mayBeReservation.get().size() == 0) {
            throw new ReserveServiceException("У места / зала с ID - " + placeId + " броней не найдено!");
        } else
            return mayBeReservation.get();
    }

    /**
     * Find reservations by slot ID.
     *
     * @param slotId slot ID to find reservation
     * @throws ReserveServiceException have no concrete slot ID in base
     * @throws ReserveServiceException if reservation by concrete slot ID is empty
     *
     * @return list of reservation for concrete slot ID
     */
    public List<Reservation> findReservationsBySlotId(Long slotId) {
        Optional<Slot> mayBeSlot = slotRepository.findSlotById(slotId);
        Optional<List<Reservation>> mayBeReservation =
                reservationRepository.findReservationBySlotId(slotId);
        if(mayBeSlot.isEmpty()){
            throw new ReserveServiceException("Слот с ID - " + slotId + " не найден!");
        } else if(mayBeReservation.isEmpty() || mayBeReservation.get().size() == 0){
            throw new ReserveServiceException("Слот с ID - " + slotId + " не забронирован!");
        } else
            return mayBeReservation.get();
    }

    /**
     * Find reservations by user ID.
     *
     * @param userId user ID to find reservation
     * @throws ReserveServiceException have no user ID in base
     * @throws ReserveServiceException if reservation base by concrete user ID is empty
     *
     * @return list of reservation
     */
    public List<Reservation> findReservationsByUserId(Long userId) {
        Optional<User> mayBeUser = userRepository.findUserById(userId);
        Optional<List<Reservation>> mayBeUserReservation =
                reservationRepository.findReservationByUserId(userId);
        if (mayBeUser.isEmpty()) {
            throw new ReserveServiceException("Пользователь с ID - " + userId +
                                              " не найден!");
        } else if(mayBeUserReservation.isEmpty() || mayBeUserReservation.get().size() == 0){
            throw new ReserveServiceException("У пользователя с ID - " + userId +
                                              " нет зарезервированных залов / рабочих мест!");
        } else
            return mayBeUserReservation.get();
    }

    /**
     * Find reservations by concrete date, place and slot.
     *
     * @param date to find reservation
     * @param placeId place ID to find reservation
     * @param slotId slot ID to find reservation
     *
     * @throws ReserveServiceException have no place ID in base
     * @throws ReserveServiceException have no slot ID in base
     * @throws ReserveServiceException have no reservation with concrete parameter in base
     *
     * @return reservation with concrete parameter
     */
    public Reservation findReservationsByDatePlaceAndSlotId(LocalDate date,
                                                            Long placeId,
                                                            Long slotId) {
        Optional<Place> mayBePlace = placeRepository.findPlaceById(placeId);
        Optional<Slot> mayBeSlot = slotRepository.findSlotById(slotId);
        Optional<Reservation> mayBeListReservation =
                reservationRepository.findReservationByDatePlaceAndSlot(date, placeId, slotId);
        if(mayBePlace.isEmpty()){
            throw new ReserveServiceException("Место / зал с ID - " + placeId + " не найден(о)!");
        } else if(mayBeSlot.isEmpty()){
            throw new ReserveServiceException("Слот с ID - " + slotId  + " не найден!");
        } else if(mayBeListReservation.isEmpty()){
            throw new ReserveServiceException("Бронь на: " + date + " с такими параметрами не найдена!");
        } else
            return mayBeListReservation.get();
    }

    /**
     * Update reservations.
     *
     * @param reservation for update reservation (new reservation with existing ID)
     *
     * @throws ReserveServiceException have no such reservation ID in base
     * @throws ReserveServiceException have no slot ID in base
     * @throws ReserveServiceException have no reservation with concrete parameter in base
     *
     * @return reservation with concrete parameter
     */
    public boolean updateReservation(Reservation reservation){
        Boolean isUpdateGood = false;
        Optional<Reservation> mayBeReservationForUpdate =
                reservationRepository.findReservationById(reservation.getReserveId());
        if(mayBeReservationForUpdate.isEmpty()) {
            throw new ReserveServiceException("Брони с ID - " + reservation.getReserveId() + " нет в БД!");
        } else if(isReservationCorrect(reservation)) {
            isUpdateGood = reservationRepository.updateReservation(reservation);
        }
        return isUpdateGood;
    }

    /**
     * Delete reservations.
     *
     * @param reservationId reservation ID
     *
     * @throws ReserveServiceException have no such reservation ID in base
     *
     * @return true if delete success
     *         false if delete fail
     */
    public boolean deleteReservation(Long reservationId){
        Optional<Reservation> mayBeReservation = reservationRepository.findReservationById(reservationId);
        if(mayBeReservation.isEmpty()){
            throw new ReserveServiceException("Бронь с ID: " + reservationId + " не найдена!");
        } else
            return reservationRepository.deleteReservation(reservationId);
    }

    /**
     * Check the entered data for correctness.
     *
     * @param reservation reservation to create/update with concrete parameters
     * @throws ReserveServiceException have no user
     * @throws ReserveServiceException have no place
     * @throws ReserveServiceException have no slot
     * @throws ReserveServiceException if try duplicate reservation
     *
     * @return true - if entered data is correct
     *         false - if entered data is not correct
     */
    private boolean isReservationCorrect(Reservation reservation){
        Optional<User> mayBeUser = userRepository.findUserById(reservation.getUser().getUserId());
        Optional<Place> mayBePlace = placeRepository.findPlaceById(reservation.getPlace().getPlaceId());
        Optional<Slot> mayBeSlot = slotRepository.findSlotById(reservation.getSlot().getSlotId());
        if(mayBeUser.isEmpty()) {
            throw new ReserveServiceException("Делающий бронь пользователь не найден в БД!");
        } else if(mayBePlace.isEmpty()) {
            throw new ReserveServiceException("Попытка забронировать несуществующий зал/место!");
        } else if(mayBeSlot.isEmpty()) {
            throw new ReserveServiceException("Попытка забронировать несуществующий слот!");
        } else if(reservationRepository.findReservationByDatePlaceAndSlot(reservation.getReserveDate(),
                reservation.getPlace().getPlaceId(),
                reservation.getSlot().getSlotId()).isPresent()) {
            throw new ReserveServiceException("'" + reservation.getPlace().getSpecies().getStrName() +
                    "' - " + reservation.getPlace().getPlaceNumber() +
                    " уже зарезервирован(о) на '" + reservation.getReserveDate() +
                    " c " + reservation.getSlot().getTimeStart() +
                    " до " + reservation.getSlot().getTimeFinish());
        } else
            return true;
    }
}