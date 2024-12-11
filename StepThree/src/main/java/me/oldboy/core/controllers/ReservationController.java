package me.oldboy.core.controllers;

import lombok.RequiredArgsConstructor;
import me.oldboy.annotations.Auditable;
import me.oldboy.annotations.Loggable;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.core.dto.reservations.ReservationCreateDto;
import me.oldboy.core.dto.reservations.ReservationReadDto;
import me.oldboy.core.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.core.model.service.PlaceService;
import me.oldboy.core.model.service.ReservationService;
import me.oldboy.core.model.service.SlotService;
import me.oldboy.core.model.service.UserService;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.exception.ReservationControllerException;
import me.oldboy.exception.ReservationServiceException;
import me.oldboy.validate.ValidatorDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final PlaceService placeService;
    private final SlotService slotService;
    private final UserService userService;

    @Loggable
    @Auditable(operationType = AuditOperationType.CREATE_RESERVATION)
    public ReservationReadDto createReservation(String userName,
                                                ReservationCreateDto createDto) throws ReservationControllerException {
        /* 1 - Валидируем входящие данные на корректность */
        ValidatorDto.getInstance().isValidData(createDto);

        /* 2 - Проверяем полученные данные на согласованность с информацией в БД */
        isPlaceCorrect(createDto.getPlaceId());
        isSlotCorrect(createDto.getSlotId());
        isReservationNotDuplicate(createDto.getReservationDate(),
                                  createDto.getPlaceId(),
                                  createDto.getSlotId());

        /*
        3 - Если на предыдущем шаге все прошло нормально, переписываем ID резервирующего пользователя независимо
            от переданных в запросе, т.е. в нашем тоталитарном коворкинг центре каждый за себя и может бронировать
            место только для себя любимого и ни для кого более. Хотя можно было сравнить данные по userId и бросить
            исключение - мол, не на себя делаешь бронирование, дорогуша.

            В данном случае проверок мы не проводим, об этом позаботился Jwt ключ переданный на слое сервлетов.
            Проверку на корректность предоставленного в ReservationCreateDto userId не проводим - все ровно заменим.
        */
        Optional<UserReadDto> reservationOwner = userService.findByUserName(userName);
        long ownerId = reservationOwner.get().userId();
        createDto.setUserId(ownerId);

        /* 4 - Пытаемся передать данные о создаваемой брони на слой сервисов */
        long reservedId = reservationService.create(createDto);

        /* Если бронирование прошло успешно возвращаем частичную запись о ней */
        return reservationService.findById(reservedId).get();
    }

    @Loggable
    @Auditable(operationType = AuditOperationType.UPDATE_RESERVATION)
    public boolean updateReservation(String userName,
                                     Boolean isAdmin,
                                     ReservationUpdateDeleteDto updateDto) throws ReservationControllerException {
        /* 1 - Валидируем входящие данные на корректность */
        ValidatorDto.getInstance().isValidData(updateDto);

        /* 2 - Проверяем полученные данные на согласованность с информацией в БД */
        isUserCorrect(updateDto.userId()); // ADMIN может передать несуществующий UserId в переданном DTO, перепроверим
        isPlaceCorrect(updateDto.placeId());
        isSlotCorrect(updateDto.slotId());
        isReservationNotDuplicate(updateDto.reservationDate(),
                                  updateDto.placeId(),
                                  updateDto.slotId());

        /* 3 - Проверяем создатель ли брони или ADMIN пытается провести обновление данных */
        canUpdateOrDelete(userName, isAdmin, updateDto);

        Boolean isUpdated = reservationService.update(updateDto);

        /* Если бронь прошла успешно возвращаем ответ */
        return isUpdated;
    }

    /*
    Для удаления брони нам необходим ее ID. Вся остальная информация,
    кроме userId, может быть условно неадекватна, но валидна.
    */
    @Loggable
    @Auditable(operationType = AuditOperationType.DELETE_RESERVATION)
    public boolean deleteReservation(String userName,
                                     Boolean isAdmin,
                                     ReservationUpdateDeleteDto deleteDto) throws ReservationControllerException {
        /* 1 - Валидируем входящие данные на корректность */
        ValidatorDto.getInstance().isValidData(deleteDto);

        /* 2 - Проверяем не пустышка ли переданный UserId */
        isUserCorrect(deleteDto.userId());

        /* 3 - Проверяем создатель ли брони или ADMIN пытается провести обновление данных */
        canUpdateOrDelete(userName, isAdmin, deleteDto);

        Boolean isDeleted = reservationService.delete(deleteDto);

        /* Если бронь прошла успешно возвращаем ответ */
        return isDeleted;
    }

    @Loggable
    public List<ReservationReadDto> readAllReservation() throws ReservationServiceException {
        return reservationService.findAll();
    }

    /**
     * Check request param and get reservation data.
     *
     * @param reservationDate the reservation date
     * @param userId the user ID whose reservations are selecting
     * @param placeId the place ID reservations we are selecting
     *
     * @return PlaceReadUpdateDto found place
     *
     * @throws NotValidArgumentException if the data has incorrect values
     */
    @Loggable
    public List<ReservationReadDto> getReservationByParam(String reservationDate,
                                                          String userId,
                                                          String placeId) throws NotValidArgumentException {
        List<ReservationReadDto> reservationByParam = null;
        try {
            if (reservationDate != null && userId == null && placeId == null) {
                reservationByParam =
                        reservationService.findByDate(convertStringDateWithValidate(reservationDate)).get().stream().toList();
            } else if (reservationDate == null && userId != null && placeId == null) {
                Long userIdToFindReservation = Long.parseLong(userId);
                if (userIdToFindReservation >= 0) {
                    reservationByParam = reservationService.findByUserId(userIdToFindReservation).get();
                } else {
                    throw new NotValidArgumentException("Check parameter - must be positive! " +
                                                        "Проверьте введенный параметр - не может быть отрицательным!");
                }
            } else if (reservationDate == null && userId == null && placeId != null){
                Long placeIdToFindReservation = Long.parseLong(placeId);
                if (placeIdToFindReservation >= 0) {
                    reservationByParam = reservationService.findByPlaceId(placeIdToFindReservation).get();
                } else {
                    throw new NotValidArgumentException("Check parameter - must be positive! " +
                                                        "Проверьте введенный параметр - не может быть отрицательным!");
                }
            } else {
                throw new NotValidArgumentException("Invalid combination of parameters (need only reservationDate or placeId or placeSpecies, not combination)! " +
                                                    "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!");
            }
        } catch (Exception e) {
            throw new NotValidArgumentException("Parse or unexpected error (check the entered parameters): " + e.getMessage());
        }
        return reservationByParam;
    }

    @Loggable
    public Map<Long, List<Long>> getFreeSlotsByDate(String date) throws ReservationControllerException,
                                                                        ReservationServiceException {
        return reservationService.findAllFreeSlotsByDate(convertStringDateWithValidate(date));
    }

    /* Вспомогательные методы - проверки валидации */

    private LocalDate convertStringDateWithValidate(String dateToParse) throws ReservationControllerException {
        if(!dateToParse.matches("^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$") || dateToParse.length() == 0){
            throw new ReservationControllerException("Date value is empty or invalid, expected, for example - 'YYYY-MM-DD'! " +
                                                     "Значение даты пустое или не верно, ожидается, например - '2007-12-03' !");
        }
        return LocalDate.parse(dateToParse);
    }

    private void canUpdateOrDelete(String ownerName,
                                   Boolean isAdmin,
                                   ReservationUpdateDeleteDto updateDeleteReservation) throws ReservationControllerException {
        /*
        Тут возникает интересная ситуация, мы с какого-то перепуга решили позволить пользователю (сервису) не обладающим
        правами ADMIN изменять "свои" записи, как проверить что именно пользователь создавший запись пытается ее изменить
        (а возможно и удалить).

        При создании брони мы насильно без ведома пользователя (сервиса) в поле userId вносим ID ее создателя, не зависимо
        от того, что было внесено в передаваемый приложению ReservationCreateDto.

        И так, у нас есть: reservationId - генерируемый БД, userId - взятый из аутентификационных данных. Именно эти данные
        не могут быть изменены из вне ни при каких обстоятельствах - опираемся на них. Либо все CRUD операции придется отдать
        в ведение пользователей (сервисов) со статусом ADMIN (а лучше одного SUPER_ADMIN - меньше проверок). И сделать для
        простых USER доступными только просмотровые операции + создание брони - все.
        */

        /* ШАГ 1 - Получаем оригинальную запись брони из БД (если таковая есть) */
        Optional<ReservationReadDto> mayBeReservation =
                reservationService.findById(updateDeleteReservation.reservationId());
        if (mayBeReservation.isEmpty()) {
            throw new ReservationControllerException("Have no reservation for update or delete! " +
                                                     "Бронь для обновления или удаления не найдена!");
        }

        /* Шаг 2 - Получаем ID аутентифицированного пользователя */
        Optional<UserReadDto> ownerData = userService.findByUserName(ownerName);

        /* ШАГ 3 - ID должен совпадать с ID из оригинальной записи брони или должен быть статус ADMIN */
        if (ownerData.get().userId() != mayBeReservation.get().userId()) {
            if (!isAdmin) {
                throw new ReservationControllerException("Have no permission to update or delete reservation! " +
                                                         "Недостаточно прав на обновление или удаление брони!");
            }
        }
    }

    /*
    Админ тоже может совершить опечатку и в БД из ReservationUpdateDelete может прилететь
    несуществующий userId. Пусть и масло масленое, но проверку на существующего в БД
    пользователя мы сделаем.
    */
    private void isUserCorrect(Long userId) throws ReservationControllerException {
        Optional<UserReadDto> mayBeUser = userService.findById(userId);
        if(mayBeUser.isEmpty()){
            throw new ReservationControllerException("Try to use non-existent userId! " +
                                                     "Применен несуществующий идентификатор пользователя!");
        }
    }

    /* Проверяем не приведет ли обновление или создание новой брони к дублированию в БД */
    private void isReservationNotDuplicate(LocalDate reservationDate,
                                           Long placeId,
                                           Long slotId) throws ReservationControllerException {
    Optional<ReservationReadDto> mayBeReservation =
                reservationService.findByDatePlaceAndSlot(reservationDate, placeId, slotId);
        if(mayBeReservation.isPresent()){
            throw new ReservationControllerException("Duplicate reservation! Дублирование брони!");
        }
    }

    /* Проверяем есть ли в БД резервируемый временной слот */
    private void isSlotCorrect(Long slotId) throws ReservationControllerException {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(slotId);
        if(mayBeSlot.isEmpty()){
            throw new ReservationControllerException("Try to use non-existent slot! " +
                                                     "Попытка использовать несуществующий слот времени!");
        }
    }

    /* Проверяем есть ли в БД резервируемое место/зал */
    private void isPlaceCorrect(Long placeId) throws ReservationControllerException {
        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(placeId);
        if(mayBePlace.isEmpty()){
            throw new ReservationControllerException("Try to use non-existent place! " +
                                                     "Попытка использовать несуществующее место/зал!");
        }
    }
}