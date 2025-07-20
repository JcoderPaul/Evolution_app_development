package me.oldboy.services;

import me.oldboy.annotations.Measurable;
import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.exception.reservation_exception.ReservationServiceException;
import me.oldboy.mapper.ReservationMapper;
import me.oldboy.models.entity.Place;
import me.oldboy.models.entity.Reservation;
import me.oldboy.models.entity.Slot;
import me.oldboy.repository.PlaceRepository;
import me.oldboy.repository.ReservationRepository;
import me.oldboy.repository.SlotRepository;
import me.oldboy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/*
Данный класс реализует следующие методы:
- создание брони;
- удаление брони;
- обновление (изменение) брони;

- поиск уникальной брони по ID;
- поиск всех доступных в БД бронирований;
- поиск уникальной брони по сочетанию даты + места + слота (reservationDate + placeId + slotId);
- поиск всех бронирований по ID пользователя (userId);
- поиск всех бронирований по ID временного слота (slotId);
- поиск всех бронирований по ID места (placeId);
*/
@Service
@Transactional(readOnly = true)
public class ReservationService {

    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    private ReservationMapper reservationMapper;

    public ReservationService(ReservationRepository reservationRepository,
                              SlotRepository slotRepository,
                              PlaceRepository placeRepository,
                              UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;

        this.reservationMapper = ReservationMapper.INSTANCE;
        reservationMapper.setSlotRepository(slotRepository);
        reservationMapper.setPlaceRepository(placeRepository);
        reservationMapper.setUserRepository(userRepository);
    }

    /**
     * Create (save) new reservation to base.
     *
     * @param reservationCreate the new reservation for creating
     * @return new created (and save to base) place ID
     */
    @Transactional
    @Measurable
    public Long create(ReservationCreateDto reservationCreate) {
        /* Валидация на уровне контроллеров */
        Optional<ReservationReadDto> mayBeReservation =
                findByDatePlaceAndSlot(reservationCreate.getReservationDate(), reservationCreate.getPlaceId(), reservationCreate.getSlotId());
        if (mayBeReservation.isEmpty()) {
            Reservation forCreateReservationWithoutId = reservationMapper.mapToEntityFromCreateDto(reservationCreate);
            Reservation createdReservation = reservationRepository.save(forCreateReservationWithoutId);
            return createdReservation.getReservationId();
        } else {
            throw new ReservationServiceException("Резервирование не возможно, измените дату,место или время.");
        }

    }

    /**
     * Update reservation.
     *
     * @param reservationUpdate the new reservation for updating
     * @return true - if update is success
     * false - if update is fail
     */
    @Transactional
    @Measurable
    public boolean update(ReservationUpdateDeleteDto reservationUpdate) {
        /* Валидация на уровне контроллеров */
        Optional<ReservationReadDto> mayBeReservation =
                findByDatePlaceAndSlot(reservationUpdate.reservationDate(), reservationUpdate.placeId(), reservationUpdate.slotId());
        if (mayBeReservation.isEmpty()) {
            Reservation forUpdateReservation = reservationMapper.mapToEntityFromUpdateDeleteDto(reservationUpdate);
            reservationRepository.save(forUpdateReservation);

            return findByDatePlaceAndSlot(reservationUpdate.reservationDate(),
                    reservationUpdate.placeId(),
                    reservationUpdate.slotId()).isPresent();
        } else {
            throw new ReservationServiceException("Обновление не возможно, дата, место или слот заняты.");
        }
    }

    /**
     * Delete reservation.
     *
     * @param reservationDelete the exists reservation delete
     * @return true - if delete is success
     * false - if delete is fail
     */
    @Transactional
    @Measurable
    public boolean delete(ReservationUpdateDeleteDto reservationDelete) {
        Optional<ReservationReadDto> mayBeReservation =
                findByDatePlaceAndSlot(reservationDelete.reservationDate(), reservationDelete.placeId(), reservationDelete.slotId());
        if (mayBeReservation.isPresent()) {
            Reservation forDeleteReserve = reservationMapper.mapToEntityFromReadDto(mayBeReservation.get());
            reservationRepository.delete(forDeleteReserve);

            return findById(mayBeReservation.get().reservationId()).isEmpty();
        } else {
            throw new ReservationServiceException("Бронь для удаления не найдена!");
        }
    }

    /**
     * Find reservation by ID.
     *
     * @param reservationId reservation ID to find
     * @return found reservationReadDto by ID
     */
    @Measurable
    public Optional<ReservationReadDto> findById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservationMapper::mapToReservationReadDto);
    }

    /**
     * Find all reservation.
     *
     * @return list of all reservation
     */
    @Measurable
    public List<ReservationReadDto> findAll() throws ReservationServiceException {
        List<Reservation> mayBeAllReservation = reservationRepository.findAll();
        if (mayBeAllReservation.isEmpty()) {
            throw new ReservationServiceException("База броней пуста!");
        } else
            return mayBeAllReservation.stream()
                    .map(reservationMapper::mapToReservationReadDto)
                    .collect(Collectors.toList());
    }

    /**
     * Find reservation by Date, Place and Slot.
     *
     * @param date    reservation date to find
     * @param placeId place ID for find reservation
     * @param slotId  slot ID for find reservation
     * @return found reservationReadDto by ID
     */
    @Measurable
    public Optional<ReservationReadDto> findByDatePlaceAndSlot(LocalDate date, Long placeId, Long slotId) {
        return reservationRepository.findByDatePlaceAndSlot(date, placeId, slotId)
                .map(reservationMapper::mapToReservationReadDto);
    }

    /**
     * Find reservation by user ID.
     *
     * @param userId user ID to find only him reservation
     * @return list of single user reservation
     */
    @Measurable
    public Optional<List<ReservationReadDto>> findByUserId(Long userId) throws ReservationServiceException {
        Optional<List<Reservation>> mayBeAllReservationByUser =
                reservationRepository.findByUserId(userId);
        if (mayBeAllReservationByUser.isEmpty() || mayBeAllReservationByUser.get().size() == 0) {
            throw new ReservationServiceException("Бронирования для пользователя с ID - " + userId + " отсутствуют!");
        } else
            return Optional.of(mayBeAllReservationByUser
                    .get()
                    .stream()
                    .map(reservationMapper::mapToReservationReadDto)
                    .collect(Collectors.toList()));
    }

    /**
     * Find reservation by slot ID.
     *
     * @param slotId slot ID to find only its reservation
     * @return list of single slot reservation
     */
    @Measurable
    public Optional<List<ReservationReadDto>> findBySlotId(Long slotId) throws ReservationServiceException {
        Optional<List<Reservation>> mayBeAllReservationBySlot =
                reservationRepository.findBySlotId(slotId);
        if (mayBeAllReservationBySlot.isEmpty() || mayBeAllReservationBySlot.get().size() == 0) {
            throw new ReservationServiceException("Бронирования для " + slotId + " отсутствуют!");
        } else
            return Optional.of(mayBeAllReservationBySlot
                    .get()
                    .stream()
                    .map(reservationMapper::mapToReservationReadDto)
                    .collect(Collectors.toList()));
    }

    /**
     * Find reservation by place ID.
     *
     * @param placeId place ID to find only its reservation
     * @return list of single place reservation
     */
    @Measurable
    public Optional<List<ReservationReadDto>> findByPlaceId(Long placeId) throws ReservationServiceException {
        Optional<List<Reservation>> mayBeAllReservationByPlace =
                reservationRepository.findByPlaceId(placeId);
        if (mayBeAllReservationByPlace.isEmpty() || mayBeAllReservationByPlace.get().size() == 0) {
            throw new ReservationServiceException("Бронирования для " + placeId + " отсутствуют!");
        } else
            return Optional.of(mayBeAllReservationByPlace
                    .get()
                    .stream()
                    .map(reservationMapper::mapToReservationReadDto)
                    .collect(Collectors.toList()));
    }

    /**
     * Find reservation by date.
     *
     * @param date date to find only its reservation
     * @return list of reservation by date
     */
    @Measurable
    public Optional<List<ReservationReadDto>> findByDate(LocalDate date) throws ReservationServiceException {
        Optional<List<Reservation>> mayBeReservationsByDate =
                reservationRepository.findByDate(date);
        if (mayBeReservationsByDate.isEmpty() || mayBeReservationsByDate.get().size() == 0) {
            throw new ReservationServiceException("Бронирования на " + date + " отсутствуют!");
        } else
            return Optional.of(mayBeReservationsByDate
                    .get()
                    .stream()
                    .map(reservationMapper::mapToReservationReadDto)
                    .collect(Collectors.toList()));
    }

    @Measurable
    public Map<Long, List<Long>> findAllFreeSlotsByDate(LocalDate date) throws ReservationServiceException {
        /* Проверяем есть ли вообще брони на заданную дату, есть дергаем список */
        Optional<List<ReservationReadDto>> reservationList = findByDate(date);

        /* Инициализируем будущую коллекцию свободных слотов раскиданных по местам на конкретную дату */
        Map<Long, List<Long>> allFreeSlotsByDateAndPlace = new HashMap<>();

        /* Получаем список ID всех слотов из базы он может меняться (временные диапазоны можно дробить, укрупнять, добавлять, удалять) */
        List<Long> allAvailableSlotFromBase =
                slotRepository.findAll()
                        .stream()
                        .map(Slot::getSlotId)
                        .toList();

        /* Получаем список ID всех места/залов из базы он может меняться (рабочие места/залы/студии и т.п. можно добавлять и удалять) */
        List<Long> allAvailablePlaceFromBase =
                placeRepository.findAll()
                        .stream()
                        .map(Place::getPlaceId)
                        .toList();

        /* Сепарируем бронирования по placeId, т.е. каждое место в выбранный день может иметь ограниченное количество зарезервированных слотов */
        Map<Long, List<ReservationReadDto>> reservationsByPlaceIdMap =
                reservationList.get()
                        .stream()
                        .collect(Collectors.groupingBy(ReservationReadDto::placeId));

        /*
        В цикле перебираем нашу просепорированную по placeId коллекцию и:
        ШАГ 1 - Для каждого placeId получаем полную коллекцию Slot-ов;
        ШАГ 2 - Для каждого placeId из полной коллекции Slot-ов удаляем зарезервированные - получаем коллекцию свободных слотов;
        ШАГ 3 - В заранее подготовленную MAP коллекцию помещаем: key = placeId и value = его коллекцию свободных слотов;
        */
        reservationsByPlaceIdMap.forEach((key, value) -> {
            List<Long> freeSlotByPlace = new ArrayList<>(allAvailableSlotFromBase);
            value.forEach(r -> freeSlotByPlace.remove(r.slotId()));
            allFreeSlotsByDateAndPlace.put(key, freeSlotByPlace);
        });

        /*
        В цикле перебираем нашу коллекцию доступных мест:
        ШАГ 4 - Коллекция свободных слотов не полная, т.к. не учтены незарезервированные на выбранный день placeId,
                добавляем их в коллекцию свободных слотов как key = placeId, в качестве value = загружаем полный
                список слотов - место на выбранный день никто не резервировал - значит все слоты свободны.
        ШАГ 5 - На выходе получаем MAP (key = placeID, value = slotCollection), если место имеет брони на выбранную
                дату его slotCollection неполная (т.к. вычтены занятые слоты), если место не имеет броней - все слоты
                свободны его slotCollection полная - мы получили ПОЛНЫЙ список свободных слотов на выбранную дату по
                всему "коворкин-центру".
        */
        allAvailablePlaceFromBase.forEach((v) -> {
            if (!allFreeSlotsByDateAndPlace.containsKey(v)) {
                allFreeSlotsByDateAndPlace.put(v, allAvailableSlotFromBase);
            }
        });

        return allFreeSlotsByDateAndPlace;
    }
}