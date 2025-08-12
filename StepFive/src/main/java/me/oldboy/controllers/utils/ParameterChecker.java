package me.oldboy.controllers.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.security_details.SecurityUserDetails;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.exception.reservation_exception.ReservationControllerException;
import me.oldboy.services.PlaceService;
import me.oldboy.services.ReservationService;
import me.oldboy.services.SlotService;
import me.oldboy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

/**
 * Class for checking and converting data received during the application's operation
 */
@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class ParameterChecker {

    @Autowired
    private UserService userService;
    @Autowired
    private PlaceService placeService;
    @Autowired
    private SlotService slotService;
    @Autowired
    private ReservationService reservationService;

    /**
     * Check user status/role/authority (USER/ADMIN)
     *
     * @param userDetails user data for check
     * @return true - user ADMIN role, false - user USER role
     */
    public boolean isAdmin(UserDetails userDetails) {
        Collection<? extends GrantedAuthority> userAuthList = ((SecurityUserDetails) userDetails).getAuthorities();
        Optional<? extends GrantedAuthority> mayBeAdmin = userAuthList.stream()
                .filter(authority -> authority.getAuthority().equals("ADMIN"))
                .findAny();
        if (mayBeAdmin.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convert received string data to LocalDate class
     *
     * @param dateToParse received string data
     * @return converted to LocalDate information
     * @throws ReservationControllerException received wrong format to convert
     */
    public LocalDate convertStringDateWithValidate(String dateToParse) {
        if (!dateToParse.matches("^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$") || dateToParse.length() == 0) {
            throw new ReservationControllerException("Значение даты пустое или не верно, ожидается, например - '2007-12-03' !");
        }
        return LocalDate.parse(dateToParse);
    }

    /**
     * Checks a delete or update operation can be applied to the retrieved data
     *
     * @param ownerName owner of operation login or user name
     * @param isAdmin the status who performs the operation
     * @param updateDeleteReservation for update or delete data
     * @throws ReservationControllerException if any exception are thrown during the checking
     */
    public void canUpdateOrDelete(String ownerName,
                                  Boolean isAdmin,
                                  ReservationUpdateDeleteDto updateDeleteReservation) {
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
            throw new ReservationControllerException("Бронь для обновления или удаления не найдена!");
        }

        /* Шаг 2 - Получаем ID аутентифицированного пользователя */
        Optional<UserReadDto> ownerData = userService.findByLogin(ownerName);

        /* ШАГ 3 - ID должен совпадать с ID из оригинальной записи брони или должен быть статус ADMIN */
        if (ownerData.get().userId() != mayBeReservation.get().userId()) {
            if (!isAdmin) {
                throw new ReservationControllerException("Недостаточно прав на обновление или удаление брони!");
            }
        }
    }

    /*
    Админ тоже может совершить опечатку и в БД из ReservationUpdateDelete может прилететь
    несуществующий userId. Пусть и масло масленое, но проверку на существующего в БД
    пользователя мы сделаем.
    */

    /**
     * Check user exists in db
     *
     * @param userId user id from checking data
     * @throws ReservationControllerException if user id not found
     */
    public void isUserCorrect(Long userId) {
        Optional<UserReadDto> mayBeUser = userService.findById(userId);
        if (mayBeUser.isEmpty()) {
            throw new ReservationControllerException("Применен несуществующий идентификатор пользователя!");
        }
    }

    /* Проверяем не приведет ли обновление или создание новой брони к дублированию в БД */

    /**
     * Check for duplicate reservation
     *
     * @param reservationDate reservation date
     * @param placeId reservation place id
     * @param slotId reservation slot id
     * @throws ReservationControllerException if reservation is duplication
     */
    public void isReservationNotDuplicate(LocalDate reservationDate,
                                          Long placeId,
                                          Long slotId) {
        Optional<ReservationReadDto> mayBeReservation =
                reservationService.findByDatePlaceAndSlot(reservationDate, placeId, slotId);
        if (mayBeReservation.isPresent()) {
            throw new ReservationControllerException("Дублирование брони!");
        }
    }

    /* Проверяем есть ли в БД резервируемый временной слот */

    /**
     * Check slot exists in db
     *
     * @param slotId slot id
     * @throws ReservationControllerException if slot id not found
     */
    public void isSlotCorrect(Long slotId) {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(slotId);
        if (mayBeSlot.isEmpty()) {
            throw new ReservationControllerException("Попытка использовать несуществующий слот времени!");
        }
    }

    /* Проверяем есть ли в БД резервируемое место/зал */

    /**
     * Check place exists in db
     *
     * @param placeId place id
     * @throws ReservationControllerException if place id not found
     */
    public void isPlaceCorrect(Long placeId) {
        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(placeId);
        if (mayBePlace.isEmpty()) {
            throw new ReservationControllerException("Попытка использовать несуществующее место/зал!");
        }
    }
}
