package me.oldboy.cwapp.handlers;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.entity.Species;
import me.oldboy.cwapp.exception.handlers_exception.ReservationHandlerException;
import me.oldboy.cwapp.services.PlaceService;
import me.oldboy.cwapp.services.ReservationService;
import me.oldboy.cwapp.services.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

@RequiredArgsConstructor
public class ReservationViewHandler {

    private final ReservationService reservationService;
    private final PlaceService placeService;
    private final UserService userService;

    /* Методы бронирования */

    public Long reservationHall(Scanner scanner, Long userId){
        System.out.println("Идет процесс бронирования конференц-зала.");

        String[] userChoice = lightMenuForCreateReservation(scanner);

        Long placeForReservationId =
                placeService.getPlaceBySpeciesAndPlaceNumber(Species.HALL,
                                                             Integer.parseInt(userChoice[1])).getPlaceId();

        return getNewReservationId(userId,
                                   placeForReservationId,
                                   LocalDate.parse(userChoice[0]),
                                   LocalTime.parse(userChoice[2]),
                                   LocalTime.parse(userChoice[3]));
    }

    public Long reservationWorkplace(Scanner scanner, Long userId){
        System.out.println("Идет процесс бронирования рабочего места.");

        String[] userChoice = lightMenuForCreateReservation(scanner);

        Long placeForReservationId =
                placeService.getPlaceBySpeciesAndPlaceNumber(Species.WORKPLACE,
                                                             Integer.parseInt(userChoice[1])).getPlaceId();

        return getNewReservationId(userId,
                                   placeForReservationId,
                                   LocalDate.parse(userChoice[0]),
                                   LocalTime.parse(userChoice[2]),
                                   LocalTime.parse(userChoice[3]));
    }

    /* Методы отмены брони */

    public boolean deleteHallReservation (Scanner scanner, Long userId){
        System.out.println("Идет процесс удаления брони.");

        String[] userChoice = lightMenuForDeleteReservation(scanner);

        Place placeForReservationDelete =
                placeService.getPlaceBySpeciesAndPlaceNumber(Species.HALL,
                                                             Integer.parseInt(userChoice[1]));

        String reserveIdForDelete = reserveIdToDelete(scanner,
                                                     userId,
                                                     userChoice[0],
                                                     placeForReservationDelete);

        return reservationService.deleteReservation(Long.parseLong(reserveIdForDelete));
    }

    public boolean deleteWorkplaceReservation (Scanner scanner, Long userId){
        System.out.println("Идет процесс удаления брони.");

        String[] userChoice = lightMenuForDeleteReservation(scanner);

        Place placeForReservationDelete =
                placeService.getPlaceBySpeciesAndPlaceNumber(Species.WORKPLACE,
                                                             Integer.parseInt(userChoice[1]));

        String reserveIdForDelete = reserveIdToDelete(scanner,
                                                     userId,
                                                     userChoice[0],
                                                     placeForReservationDelete);

        return reservationService.deleteReservation(Long.parseLong(reserveIdForDelete));
    }

    /* Просмотровые методы */

    public void showAllReservation(){
        reservationService.findAllReservation()
                .forEach(res -> showReservationToScreen(res));
    }

    public void showAllReservationByDate(Scanner scanner){
        System.out.print("Введите дату на которую хотите посмотреть все брони (yyyy-mm-dd): ");
        String findReserveDate = scanner.nextLine();

        reservationService.findReservationByDate(LocalDate.parse(findReserveDate))
                .forEach(res -> showReservationToScreen(res));
    }

    public void showAllReservationByUserId(Long userId){
        reservationService.findReservationByUserId(userId)
                .forEach(res -> showReservationToScreen(res));
    }

    public void showAllReservationByPlaceId(Scanner scanner){
        System.out.print("Введите ID рабочего места/зала информацию по которому вы хотите посмотреть: ");
        String findReserveByPlaceId = scanner.nextLine();

        reservationService.findReservationByPlaceId(Long.parseLong(findReserveByPlaceId))
                .forEach(res -> showReservationToScreen(res));
    }

    /* Служебные методы */

    private void showReservationToScreen(Reservation reservation) {
        System.out.println("Бронь с ID - " + reservation.getReservationId() +
                " на " + reservation.getReservationDate() +
                " сделана на: " + placeService.getPlaceById(reservation.getReservationPlaceId()) +
                " на время " + reservation.getStartTime() +
                " - " + reservation.getFinishTime() +
                " принадлежит: " +
                userService.getExistUserById(reservation.getReservationUserId()));
    }

    private Long getNewReservationId(Long userId,
                                     Long placeForReservationId,
                                     LocalDate reservationDate,
                                     LocalTime startReservationTime,
                                     LocalTime finishReservationTime) {

        Reservation nowReservation = null;
        Long clientUserId = userService.getExistUserById(userId)
                                       .getUserId();

        if(placeForReservationId == null && clientUserId == null){
            throw new ReservationHandlerException("Неверно переданы место резервирования/пользователь!");
        } else {
            nowReservation = new Reservation(reservationDate,
                                             placeForReservationId,
                                             clientUserId,
                                             startReservationTime,
                                             finishReservationTime);
        }
        if(reservationService.isReservationConflict(nowReservation)){
            throw new ReservationHandlerException("Конфликт времени резервирования!");
        } else
            return reservationService.createReservation(nowReservation);
    }

    private String reserveIdToDelete(Scanner scanner,
                                     Long userId,
                                     String date,
                                     Place placeForReservationDelete) {

        Long clientUserId = userService.getExistUserById(userId)
                                       .getUserId();
        List<Reservation> mayBeReservationList =
                reservationService.findReservationByDateAndPlace(LocalDate.parse(date),
                                                                 placeForReservationDelete.getPlaceId());
        List<Reservation> thisUserReserve = mayBeReservationList.stream()
                .filter(r->r.getReservationUserId().equals(userId))
                .toList();

        if (mayBeReservationList.isEmpty() || thisUserReserve.isEmpty() || clientUserId == 0){
            throw new ReservationHandlerException("Броней с указанными параметрами не найдено!");
        } else {
            System.out.println("Список бронирований: ");
            thisUserReserve.forEach(reservation -> {
                System.out.println("Бронь с ID - " + reservation.getReservationId() +
                        " на " + date +
                        " сделана на: " + placeForReservationDelete.getSpecies().toString() +
                        " номер " + placeForReservationDelete.getPlaceNumber() +
                        " на время " + reservation.getStartTime() +
                        " - " + reservation.getFinishTime());
            });
        }

        System.out.print("\nВыберите номер брони для удаления: ");
        return scanner.nextLine();
    }

    private String[] lightMenuForCreateReservation(Scanner scanner){
        String[] userChoice = new String[4];

        System.out.print("Введите дату резервирования (yyyy-mm-dd): ");
        String enterReservationDate = scanner.nextLine().trim();
        userChoice[0] = enterReservationDate;
        System.out.print("Введите номер зала/место, который планируете бронировать: ");
        String enterPlaceNumber = scanner.nextLine().trim();
        userChoice[1] = enterPlaceNumber;
        System.out.print("Введите время резервирования с (hh:mm): ");
        String enterStartTime = scanner.nextLine().trim();
        userChoice[2] = enterStartTime;
        System.out.print("Введите время резервирования по (hh:mm): ");
        String enterFinishTime = scanner.nextLine().trim();
        userChoice[3] = enterFinishTime;

        return userChoice;
    }

    private String[] lightMenuForDeleteReservation(Scanner scanner){
        String[] userChoice = new String[2];

        System.out.print("Введите дату брони (yyyy-mm-dd): ");
        String reserveDate = scanner.nextLine();
        userChoice[0] = reserveDate;
        System.out.print("Введите номер места/зала бронь которого вы ходите отменить: ");
        String reservePlaceId = scanner.nextLine();
        userChoice[1] = reservePlaceId;

        return userChoice;
    }
}
