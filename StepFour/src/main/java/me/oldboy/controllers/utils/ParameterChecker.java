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

    public LocalDate convertStringDateWithValidate(String dateToParse) throws ReservationControllerException {
        if (!dateToParse.matches("^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$") || dateToParse.length() == 0) {
            throw new ReservationControllerException("Значение даты пустое или не верно, ожидается, например - '2007-12-03' !");
        }
        return LocalDate.parse(dateToParse);
    }

    public void canUpdateOrDelete(String ownerName,
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
    public void isUserCorrect(Long userId) throws ReservationControllerException {
        Optional<UserReadDto> mayBeUser = userService.findById(userId);
        if (mayBeUser.isEmpty()) {
            throw new ReservationControllerException("Применен несуществующий идентификатор пользователя!");
        }
    }

    /* Проверяем не приведет ли обновление или создание новой брони к дублированию в БД */
    public void isReservationNotDuplicate(LocalDate reservationDate,
                                          Long placeId,
                                          Long slotId) throws ReservationControllerException {
        Optional<ReservationReadDto> mayBeReservation =
                reservationService.findByDatePlaceAndSlot(reservationDate, placeId, slotId);
        if (mayBeReservation.isPresent()) {
            throw new ReservationControllerException("Дублирование брони!");
        }
    }

    /* Проверяем есть ли в БД резервируемый временной слот */
    public void isSlotCorrect(Long slotId) throws ReservationControllerException {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(slotId);
        if (mayBeSlot.isEmpty()) {
            throw new ReservationControllerException("Попытка использовать несуществующий слот времени!");
        }
    }

    /* Проверяем есть ли в БД резервируемое место/зал */
    public void isPlaceCorrect(Long placeId) throws ReservationControllerException {
        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(placeId);
        if (mayBePlace.isEmpty()) {
            throw new ReservationControllerException("Попытка использовать несуществующее место/зал!");
        }
    }
}
