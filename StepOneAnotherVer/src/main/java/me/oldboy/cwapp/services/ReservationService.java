package me.oldboy.cwapp.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.exception.service_exception.ReservationServiceException;
import me.oldboy.cwapp.store.base.ReservationBase;
import me.oldboy.cwapp.store.repository.PlaceRepository;
import me.oldboy.cwapp.store.repository.ReservationRepository;
import me.oldboy.cwapp.store.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReservationService {

    private ReservationRepository reservationRepository;
    private PlaceRepository placeRepository;
    private UserRepository userRepository;

    public Long createReservation(Reservation newReservation){
        Optional<List<Reservation>> mayBeReservationList =
                reservationRepository.findByDateAndPlace(newReservation.getReservationDate(),
                                                         newReservation.getReservationPlaceId());
        if(mayBeReservationList.isPresent() && reservationRepository.isReservationConflict(newReservation)){
            throw new ReservationServiceException("Выбранный временной отрезок занят! " +
                                                  "Конфликт резервирования!");
        } else
            return reservationRepository.create(newReservation);
    }

    public Reservation findReservationById(Long reservationId){
        Optional<Reservation> mayBeReservation = reservationRepository.findById(reservationId);
        if (mayBeReservation.isEmpty()){
            throw new ReservationServiceException("Резервирование с Id - " + reservationId + " не найдено!");
        } else
            return mayBeReservation.get();
    }

    public List<Reservation> findReservationByDate(LocalDate reservationDate){
        Optional<List<Reservation>> mayBeList = reservationRepository.findByDate(reservationDate);
        if(mayBeList.isEmpty()){
            throw new ReservationServiceException("На " + reservationDate + " бронирования отсутствуют!");
        } else
            return mayBeList.get();
    }

    public List<Reservation> findReservationByUserId(Long userId){
        Optional<List<Reservation>> mayBeList = reservationRepository.findByUserId(userId);
        if(mayBeList.isEmpty()){
            throw new ReservationServiceException("Пользователь с ID - " + userId + " бронирований не имеет!");
        } else
            return mayBeList.get();
    }

    public List<Reservation> findReservationByPlaceId(Long placeId){
        Optional<List<Reservation>> mayBeFindList = reservationRepository.findByPlaceId(placeId);
        if(mayBeFindList.isEmpty()){
            throw new ReservationServiceException("Рабочее место/зал с ID - " + placeId + " не забронированы!");
        } else
            return mayBeFindList.get();
    }

    public List<Reservation> findReservationByDateAndPlace(LocalDate reservationDate, Long reservationPlaceId){
       Optional<List<Reservation>> mayByeReservationList =
               reservationRepository.findByDateAndPlace(reservationDate, reservationPlaceId);
       if(mayByeReservationList.isEmpty()){
           throw new ReservationServiceException("Рабочее место/зал с ID - " + reservationPlaceId +
                                                 " не имеет броней на: " + reservationDate);
       } else
           return mayByeReservationList.get();
    }

    public List<Reservation> findAllReservation(){
        List<Reservation> isListNotEmpty = reservationRepository.findAll();
        if(isListNotEmpty.isEmpty()){
            throw new ReservationServiceException("База броней пуста / ошибка связи с БД!");
        } else
            return isListNotEmpty;
    }

    public Reservation updateReservation(Reservation reservationForUpdate){
        Optional<Reservation> isReservationAlreadyHas =
                reservationRepository.findById(reservationForUpdate.getReservationId());
        if (isReservationAlreadyHas.isPresent() &&
                reservationRepository.isReservationConflict(reservationForUpdate)) {
            throw new ReservationServiceException("Обновление резервирования невозможно! " +
                                                  "Конфликт диапазонов!");
        } else
            return reservationRepository.update(reservationForUpdate);
    }

    public boolean deleteReservation(Long reservationId) {
        Optional<Reservation> mayBeReservation = reservationRepository.findById(reservationId);
        if(mayBeReservation.isEmpty()){
            throw new ReservationServiceException("Брони с ID - " + reservationId + " не существует!");
        } else
            return reservationRepository.delete(reservationId);
    }
}
