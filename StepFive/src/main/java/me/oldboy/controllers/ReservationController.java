package me.oldboy.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.auditor.core.annotation.Auditable;
import me.oldboy.auditor.core.entity.operations.AuditOperationType;
import me.oldboy.controllers.utils.ParameterChecker;
import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.exception.reservation_exception.ReservationControllerException;
import me.oldboy.exception.reservation_exception.ReservationServiceException;
import me.oldboy.services.PlaceService;
import me.oldboy.services.ReservationService;
import me.oldboy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private final ReservationService reservationService;
    @Autowired
    private final PlaceService placeService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final ParameterChecker parameterChecker;

    @Auditable(operationType = AuditOperationType.CREATE_RESERVATION)
    @PostMapping("/create")
    public ResponseEntity<ReservationReadDto> createReservation(@Validated
                                                                @RequestBody
                                                                ReservationCreateDto createDto,
                                                                @AuthenticationPrincipal
                                                                UserDetails userDetails) throws ReservationControllerException {

        String currentLogin = userDetails.getUsername();

        /* 1 - Проверяем полученные данные на согласованность с информацией в БД */
        parameterChecker.isUserCorrect(createDto.getUserId());   // В рамках текущего метода такая проверка ненужна, но мы ее тут оставим и протестируем
        parameterChecker.isPlaceCorrect(createDto.getPlaceId());
        parameterChecker.isSlotCorrect(createDto.getSlotId());
        parameterChecker.isReservationNotDuplicate(createDto.getReservationDate(),
                createDto.getPlaceId(),
                createDto.getSlotId());

        /*
        2 - Если на предыдущем шаге все прошло нормально, переписываем ID резервирующего пользователя
        независимо от переданных в запросе, т.е. каждый только за себя может бронировать. Поскольку
        пользователь в системе, то мы не проверяем состояние Optional на *.isEmpty()
        */
        Optional<UserReadDto> reservationOwner = userService.findByLogin(currentLogin);
        long ownerId = reservationOwner.get().userId();
        createDto.setUserId(ownerId);

        /* 3 - Пытаемся передать данные о создаваемой брони на слой сервисов */
        long reservedId = reservationService.create(createDto);

        /* Если бронирование прошло успешно возвращаем частичную запись о ней */
        return ResponseEntity.ok().body(reservationService.findById(reservedId).get());
    }

    @Auditable(operationType = AuditOperationType.UPDATE_RESERVATION)
    @PostMapping("/update")
    public ResponseEntity<?> updateReservation(@Validated
                                               @RequestBody
                                               ReservationUpdateDeleteDto updateDto,
                                               @AuthenticationPrincipal
                                               UserDetails userDetails) throws ReservationControllerException {

        String currentLogin = userDetails.getUsername();

        /* 1 - Проверяем полученные данные на согласованность с информацией в БД */
        parameterChecker.isUserCorrect(updateDto.userId()); // При обновлении можно передать несуществующий UserId в переданном DTO, перепроверим
        parameterChecker.isPlaceCorrect(updateDto.placeId());
        parameterChecker.isSlotCorrect(updateDto.slotId());
        parameterChecker.isReservationNotDuplicate(updateDto.reservationDate(),
                updateDto.placeId(),
                updateDto.slotId());

        /* 2 - Проверяем создатель ли брони или ADMIN пытается провести обновление данных */
        parameterChecker.canUpdateOrDelete(currentLogin, parameterChecker.isAdmin(userDetails), updateDto);

        reservationService.update(updateDto);

        /* 3 - Если бронь прошла успешно возвращаем ответ */
        return ResponseEntity.ok().body("{\"message\": \"Reservation updated\"}");
    }

    /*
    Для удаления брони нам необходим ее ID. Вся остальная информация,
    кроме userId, может быть условно неадекватна, но валидна.
    */
    @Auditable(operationType = AuditOperationType.DELETE_RESERVATION)
    @PostMapping("/delete")
    public ResponseEntity<?> deleteReservation(@Validated
                                               @RequestBody
                                               ReservationUpdateDeleteDto deleteDto,
                                               @AuthenticationPrincipal
                                               UserDetails userDetails) throws ReservationControllerException {

        String currentLogin = userDetails.getUsername();

        /* 1 - Проверяем не пустышка ли переданный UserId */
        parameterChecker.isUserCorrect(deleteDto.userId());

        /* 2 - Проверяем создатель ли брони или ADMIN пытается провести удаление данных */
        parameterChecker.canUpdateOrDelete(currentLogin, parameterChecker.isAdmin(userDetails), deleteDto);

        reservationService.delete(deleteDto);

        /* 3 - Если бронь прошла успешно возвращаем ответ */
        return ResponseEntity.ok().body("{\"message\": \"Reservation removed\"}");
    }

    @GetMapping()
    public List<ReservationReadDto> readAllReservation() throws ReservationServiceException {
        return reservationService.findAll();
    }

    /**
     * Check request param and get reservation data.
     *
     * @param reservationDate the reservation date
     * @param userId          the user ID whose reservations are selecting
     * @param placeId         the place ID reservations we are selecting
     * @return PlaceReadUpdateDto found place
     * @throws NotValidArgumentException if the data has incorrect values
     */
    @GetMapping("/booked")
    public List<ReservationReadDto> getReservationByParam(@RequestParam(value = "reservationDate", required = false)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                          LocalDate reservationDate,
                                                          @RequestParam(value = "userId", required = false)
                                                          Long userId,
                                                          @RequestParam(value = "placeId", required = false)
                                                          Long placeId) {
        List<ReservationReadDto> reservationByParam = null;
        try {
            if (reservationDate != null && userId == null && placeId == null) {
                reservationByParam = reservationService.findByDate(reservationDate).get();
            } else if (reservationDate == null && userId != null && placeId == null) {
                if (userId >= 0 & userService.findById(userId).isPresent()) {
                    reservationByParam = reservationService.findByUserId(userId).get();
                } else {
                    throw new NotValidArgumentException("Введенный параметр не найден в БД или отрицательный!");
                }
            } else if (reservationDate == null && userId == null && placeId != null) {
                if (placeId >= 0 & placeService.findById(placeId).isPresent()) {
                    reservationByParam = reservationService.findByPlaceId(placeId).get();
                } else {
                    throw new NotValidArgumentException("Введенный параметр не найден в БД или отрицательный!");
                }
            } else {
                throw new NotValidArgumentException("Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!");
            }
        } catch (Exception e) {
            throw new NotValidArgumentException("Parse or unexpected error (check the entered parameters): " + e.getMessage());
        }
        return reservationByParam;
    }

    @GetMapping("/free/date/{date}")
    public Map<Long, List<Long>> getFreeSlotsByDate(@PathVariable("date") String date) throws ReservationControllerException, ReservationServiceException {
        return reservationService.findAllFreeSlotsByDate(parameterChecker.convertStringDateWithValidate(date));
    }
}