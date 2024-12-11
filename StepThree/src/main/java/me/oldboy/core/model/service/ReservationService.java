package me.oldboy.core.model.service;

import me.oldboy.core.dto.reservations.ReservationCreateDto;
import me.oldboy.core.dto.reservations.ReservationReadDto;
import me.oldboy.core.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.repository.PlaceRepository;
import me.oldboy.core.model.database.repository.SlotRepository;
import me.oldboy.core.model.database.repository.UserRepository;
import me.oldboy.exception.ReservationServiceException;
import me.oldboy.core.mapper.ReservationMapper;
import me.oldboy.core.model.database.entity.Reservation;
import me.oldboy.core.model.database.repository.ReservationRepository;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;

import javax.transaction.Transactional;
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

public class ReservationService extends ServiceBase<Long, Reservation>{

    private SlotRepository slotRepository;
    private PlaceRepository placeRepository;
    private UserRepository userRepository;
    private ReservationMapper reservationMapper;
    public ReservationService(RepositoryBase<Long, Reservation> repositoryBase,
                              SlotRepository slotRepository,
                              PlaceRepository placeRepository,
                              UserRepository userRepository) {
        super(repositoryBase);
        this.slotRepository = slotRepository;
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
        reservationMapper = ReservationMapper.INSTANCE;
        reservationMapper.setSlotRepository(slotRepository);
        reservationMapper.setPlaceRepository(placeRepository);
        reservationMapper.setUserRepository(userRepository);
    }

    /**
     * Create (save) new reservation to base.
     *
     * @param reservationCreate the new reservation for creating
     *
     * @return new created (and save to base) place ID
     */
    @Transactional
    public Long create(ReservationCreateDto reservationCreate){
        /* Валидация и другие проверки на уровне контроллеров */
        Reservation forCreateReservationWithoutId = reservationMapper.mapToEntityFromCreateDto(reservationCreate);
        Reservation createdReservation = getRepositoryBase().create(forCreateReservationWithoutId);
        return createdReservation.getReservationId();
    }

    /**
     * Update reservation.
     *
     * @param reservationUpdate the new reservation for updating
     *
     * @return true - if update is success
     *         false - if update is fail
     */
    @Transactional
    public boolean update(ReservationUpdateDeleteDto reservationUpdate){
        /* Валидация и другие проверки на уровне контроллеров */
        Reservation forUpdateReservation = reservationMapper.mapToEntityFromUpdateDeleteDto(reservationUpdate);
        getRepositoryBase().update(forUpdateReservation);
        return findByDatePlaceAndSlot(reservationUpdate.reservationDate(),
                                      reservationUpdate.placeId(),
                                      reservationUpdate.slotId()).isPresent();
    }

    /**
     * Delete reservation.
     *
     * @param reservationDelete the exists reservation delete
     *
     * @return true - if delete is success
     *         false - if delete is fail
     */
    @Transactional
    public boolean delete(ReservationUpdateDeleteDto reservationDelete){
        /* Валидация и другие проверки на уровне контроллеров */
        getRepositoryBase().delete(reservationDelete.reservationId());
        return findById(reservationDelete.reservationId()).isEmpty();
    }

    /**
     * Find reservation by ID.
     *
     * @param reservationId reservation ID to find
     *
     * @return found reservationReadDto by ID
     */
    @Transactional
    public Optional<ReservationReadDto> findById(Long reservationId) {
        return getRepositoryBase().findById(reservationId)
                .map(reservationMapper::mapToReservationReadDto);
    }

    /**
     * Find all reservation.
     *
     * @return list of all reservation
     */
    @Transactional
    public List<ReservationReadDto> findAll() throws ReservationServiceException {
        List<Reservation> mayBeAllReservation = getRepositoryBase().findAll();
        if(mayBeAllReservation.isEmpty()){
            throw new ReservationServiceException("База броней пуста!");
        } else
            return mayBeAllReservation.stream()
                                      .map(reservationMapper::mapToReservationReadDto)
                                      .collect(Collectors.toList());
    }

    /**
     * Find reservation by Date, Place and Slot.
     *
     * @param date reservation date to find
     * @param placeId place ID for find reservation
     * @param slotId slot ID for find reservation
     *
     * @return found reservationReadDto by ID
     */
    @Transactional
    public Optional<ReservationReadDto> findByDatePlaceAndSlot(LocalDate date, Long placeId, Long slotId) {
        return ((ReservationRepository) getRepositoryBase()).findReservationByDatePlaceAndSlot(date, placeId, slotId)
                .map(reservationMapper::mapToReservationReadDto);
    }

    /**
     * Find reservation by user ID.
     *
     * @param userId user ID to find only him reservation
     *
     * @return list of single user reservation
     */
    @Transactional
    public Optional<List<ReservationReadDto>> findByUserId(Long userId) throws ReservationServiceException {
        Optional<List<Reservation>> mayBeAllReservationByUser =
                ((ReservationRepository) getRepositoryBase()).findReservationByUserId(userId);
        if(mayBeAllReservationByUser.isEmpty() || mayBeAllReservationByUser.get().size() == 0){
            throw new ReservationServiceException("There are no reservations for user with ID - " + userId + "! " +
                                                  "Бронирования для пользователя с ID - " + userId + " отсутствуют!");
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
     *
     * @return list of single slot reservation
     */
    @Transactional
    public Optional<List<ReservationReadDto>> findBySlotId(Long slotId) throws ReservationServiceException {
        Optional<List<Reservation>> mayBeAllReservationBySlot =
                ((ReservationRepository) getRepositoryBase()).findReservationBySlotId(slotId);
        if(mayBeAllReservationBySlot.isEmpty() || mayBeAllReservationBySlot.get().size() == 0){
            throw new ReservationServiceException("There are no reservations for " + slotId + "! " +
                                                  "Бронирования для " + slotId + " отсутствуют!");
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
     *
     * @return list of single place reservation
     */
    @Transactional
    public Optional<List<ReservationReadDto>> findByPlaceId(Long placeId) throws ReservationServiceException {
        Optional<List<Reservation>> mayBeAllReservationByPlace =
                ((ReservationRepository) getRepositoryBase()).findReservationByPlaceId(placeId);
        if(mayBeAllReservationByPlace.isEmpty() || mayBeAllReservationByPlace.get().size() == 0){
            throw new ReservationServiceException("There are no reservations for " + placeId + "! " +
                                                  "Бронирования для " + placeId + " отсутствуют!");
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
     *
     * @return list of reservation by date
     */
    @Transactional
    public Optional<List<ReservationReadDto>> findByDate(LocalDate date) throws ReservationServiceException {
        Optional<List<Reservation>> mayBeReservationsByDate =
                ((ReservationRepository) getRepositoryBase()).findReservationByDate(date);
        if(mayBeReservationsByDate.isEmpty() || mayBeReservationsByDate.get().size() == 0){
            throw new ReservationServiceException("There are no reservations for " + date + "! " +
                                                  "Бронирования на " + date + " отсутствуют!");
        } else
            return Optional.of(mayBeReservationsByDate
                    .get()
                    .stream()
                    .map(reservationMapper::mapToReservationReadDto)
                    .collect(Collectors.toList()));
    }

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
            if (!allFreeSlotsByDateAndPlace.containsKey(v)){
                allFreeSlotsByDateAndPlace.put(v, allAvailableSlotFromBase);
            }
        });

        return allFreeSlotsByDateAndPlace;
    }
}