package me.oldboy.cwapp.store.base;

import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.exception.base_exception.ReservationBaseException;
import me.oldboy.cwapp.store.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReservationBase implements ReservationRepository {

    private Map<Long, Reservation> reservationBase = new HashMap<>();

    /**
     * Создает новую бронь.
     *
     * Генерация ID идет на старте исходя из размера БД,
     * затем исходя из максимального значения ID имеющегося в БД.
     *
     * @param createReservation бронь с конкретными параметрами для создания записи в БД
     * @throws ReservationBaseException если произошла ошибка генерации ID
     * @throws ReservationBaseException если произошла ошибка создания новой брони
     * @return значение ID созданной брони
     */
    @Override
    public Long create(Reservation createReservation) {
        Long generateId = null;
        if(createReservation.getReservationId() == null && reservationBase.size() == 0){
            generateId = 1L;
            createReservation.setReservationId(generateId);
            reservationBase.put(generateId, createReservation);
        } else if (createReservation.getReservationId() == null && reservationBase.size() > 0){
            generateId = 1L + reservationBase.keySet()
                                             .stream()
                                             .mapToLong(key->key)
                                             .max()
                                             .orElseThrow(() -> new ReservationBaseException("Сбой в генерации ID!"));
            createReservation.setReservationId(generateId);
            reservationBase.put(generateId, createReservation);
        } else {
            throw new ReservationBaseException("Ошибка резервирования!");
        }
        return generateId;
    }

    /**
     * Ищет бронь в БД по ее ID.
     *
     * @param reservationId ID брони для поиска в БД
     * @return возможное найденное значение брони по ID
     */
    @Override
    public Optional<Reservation> findById(Long reservationId) {
        return Optional.ofNullable(reservationBase.get(reservationId));
    }

    /**
     * Обновляет существующую бронь.
     *
     * @param reservation бронь данные которой обновляются в БД
     * @throws ReservationBaseException если произошла ошибка обновления брони
     * @return значение обновленной брони
     */
    @Override
    public Reservation update(Reservation reservation) {
        if(reservationBase.containsKey(reservation.getReservationId())) {
            delete(reservation.getReservationId());
            reservationBase.put(reservation.getReservationId(), reservation);
        } else {
            throw new ReservationBaseException("Вы пытаетесь обновить несуществующею бронь!");
        }
        return reservationBase.get(reservation.getReservationId());
    }

    /**
     * Удаляет существующую бронь из БД.
     *
     * @param reservationId ID брони для удаления из БД
     * @throws ReservationBaseException если ID брони в БД не найдено
     * @return true - при удачном удалении брони, false - при провале операции удаления
     */
    @Override
    public boolean delete(Long reservationId) {
        if(reservationBase.containsKey(reservationId)){
            reservationBase.remove(reservationId);
        } else {
            throw new ReservationBaseException("Бронь с ID: " + reservationId + " в базе не найдена!");
        }
        return true;
    }

    /**
     * Ищет все брони по заданной дате.
     *
     * @param reservationDate дата брони для поиска в БД
     * @return возможный список всех броней с заданной датой для поиска
     */
    @Override
    public Optional<List<Reservation>> findByDate(LocalDate reservationDate) {
        return Optional.of(reservationBase.values().stream()
                .filter(u -> u.getReservationDate().equals(reservationDate))
                .collect(Collectors.toList()));
    }

    /**
     * Ищет все брони по ID user-a.
     *
     * @param userId ID user-a все брони которого ищем в БД
     * @return возможный список всех броней с заданным ID user-a
     */
    @Override
    public Optional<List<Reservation>> findByUserId(Long userId) {
        return Optional.of(reservationBase.values().stream()
                .filter(u -> u.getReservationUserId().equals(userId))
                .collect(Collectors.toList()));
    }

    /**
     * Ищет все брони по ID place-a.
     *
     * @param placeId ID place-a все брони которого ищем в БД
     * @return возможный список всех броней с заданным ID place-a
     */
    @Override
    public Optional<List<Reservation>> findByPlaceId(Long placeId) {
        return Optional.of(reservationBase.values().stream()
                .filter(u -> u.getReservationPlaceId().equals(placeId))
                .collect(Collectors.toList()));
    }

    /**
     * Ищет все брони по дате и ID place-a.
     *
     * @param reservationDate дата брони которую ищем в БД
     * @param placeId ID place-a все брони которого ищем в БД
     * @return возможный список всех броней с заданной датой и ID place-a
     */
    @Override
    public Optional<List<Reservation>> findByDateAndPlace(LocalDate reservationDate,
                                                          Long placeId) {
        return Optional.of(reservationBase.values()
                        .stream()
                        .filter(r -> r.getReservationDate().equals(reservationDate) &&
                                     r.getReservationPlaceId().equals(placeId))
                        .collect(Collectors.toList()));
    }

    /**
     * Ищет абсолютно все брони в БД резервирования.
     *
     * @return список всех броней
     */
    @Override
    public List<Reservation> findAll() {
        return reservationBase.entrySet().stream()
                .map(r -> r.getValue())
                .collect(Collectors.toList());
    }

    public Map<Long, Reservation> getReservationBase() {
        return reservationBase;
    }

    /**
     * Проверяет, существует ли конфликт бронирования с другими записями броней из БД.
     *
     * @param newReservation новая бронь конфликт которой мы хотим проверить с уже существующими
     * @return true - если конфликт при бронировании есть, false - если конфликт при бронировании не обнаружен
     */
    public boolean isReservationConflict(Reservation newReservation){
        return findByDateAndPlace(newReservation.getReservationDate(),
                                   newReservation.getReservationPlaceId())
                .stream()
                .flatMap(r -> r.stream())
                .anyMatch(existReservation ->
                        existReservation.getStartTime().isBefore(newReservation.getFinishTime()) &&
                                existReservation.getFinishTime().isAfter(newReservation.getStartTime()));
    }
}