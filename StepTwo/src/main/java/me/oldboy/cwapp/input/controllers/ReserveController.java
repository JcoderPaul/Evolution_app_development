package me.oldboy.cwapp.input.controllers;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.exceptions.controllers.ReserveControllerException;
import me.oldboy.cwapp.input.entity.*;
import me.oldboy.cwapp.input.service.PlaceService;
import me.oldboy.cwapp.input.service.ReserveService;
import me.oldboy.cwapp.input.service.SlotService;
import me.oldboy.cwapp.input.service.UserService;

import java.time.LocalDate;
import java.util.Scanner;

@RequiredArgsConstructor
public class ReserveController {

    private final ReserveService reservationService;
    private final PlaceService placeService;
    private final SlotService slotService;
    private final UserService userService;

    /* Метод бронирования */

    public Long reservationPlace(Scanner scanner, Long userId){
        System.out.println("Идет процесс создания брони.");

        String[] userChoice = lightMenuForCreateAndDeleteReservation(scanner); // Метод-меню для выбора параметров брони
        Species userChoiceSpecies = choiceSpecies(userChoice[0]); // Метод-меню для выбора специфики ресурса брони

        User userReservation = userService.findUserById(userId);
        Place placeForReservation =
                placeService.findPlaceBySpeciesAndNumber(userChoiceSpecies, Integer.parseInt(userChoice[2]));
        Slot slotForReservation =
                slotService.findSlotByNumber(Integer.parseInt(userChoice[3]));

        return generatedReservationId(LocalDate.parse(userChoice[1]),
                                      userReservation,
                                      placeForReservation,
                                      slotForReservation);
    }

    /* Метод отмены бронирования */

    public boolean deletePlaceReservation (Scanner scanner, Long userId){
        System.out.println("Идет процесс удаления брони.");

        String[] userChoice = lightMenuForCreateAndDeleteReservation(scanner); // Метод-меню для выбора параметров брони
        Species userChoiceSpecies = choiceSpecies(userChoice[0]); // Метод-меню для выбора специфики ресурса брони

        User userReservation = userService.findUserById(userId);
        Place placeForDeleteReservation =
                placeService.findPlaceBySpeciesAndNumber(userChoiceSpecies, Integer.parseInt(userChoice[2]));
        Slot slotForReservationDelete =
                slotService.findSlotByNumber(Integer.parseInt(userChoice[3]));

        Long reservationIdForDelete = getReservationIdForDelete(LocalDate.parse(userChoice[1]),
                                                                userReservation,
                                                                placeForDeleteReservation,
                                                                slotForReservationDelete);

        return reservationService.deleteReservation(reservationIdForDelete);
    }
   
    /* Методы для просмотра (поиск броней) */

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
        reservationService.findReservationsByUserId(userId)
                .forEach(res -> showReservationToScreen(res));
    }

    public void showAllReservationByPlaceId(Scanner scanner){
        System.out.print("Введите ID рабочего места/зала информацию по которому вы хотите посмотреть: ");
        String findReserveByPlaceId = scanner.nextLine();

        reservationService.findReservationByPlaceId(Long.parseLong(findReserveByPlaceId))
                .forEach(res -> showReservationToScreen(res));
    }

    /* Служебный метод для отображения всех броней на экран */

    private void showReservationToScreen(Reservation reservation) {
        System.out.println("Бронь ID - " + reservation.getReserveId() +
                           " на " + reservation.getReserveDate() +
                           " зарезервирован(о): " + reservation.getPlace() +
                           " слот: " + reservation.getSlot() +
                           " принадлежит: " + reservation.getUser());
    }

    /* Служебные методы для создания брони */
    private Long generatedReservationId(LocalDate reservationDate, User user, Place place, Slot slot) {
        Reservation nowReservation = null;
        Reservation mayBeReservationExist =
                reservationService.findReservationsByDatePlaceAndSlotId(reservationDate,
                                                                        place.getPlaceId(),
                                                                        slot.getSlotId());
        if(mayBeReservationExist != null) {
            throw new ReserveControllerException("Повторное резервирование места и слота на ту же дату недопустимо!");
        } else {
            nowReservation = new Reservation(reservationDate, user, place, slot);
        }
        return reservationService.createReservation(nowReservation);
    }

    /* Служебные методы для удаления брони */

    private Long getReservationIdForDelete(LocalDate date, User user, Place place, Slot slot) {
        Reservation mayBeReservation =
                reservationService.findReservationsByDatePlaceAndSlotId(date, place.getPlaceId(), slot.getSlotId());
        if (mayBeReservation == null){
            throw new ReserveControllerException("Брони с указанными параметрами не найдено!");
        } else
            return mayBeReservation.getReserveId();
    }

    /* Служебное меню для создания и удаления брони */
    private String[] lightMenuForCreateAndDeleteReservation(Scanner scanner){
        String[] userChoice = new String[4];

        System.out.print("Выберите ресурс 'зал' или 'место' для создания / удаления брони: ");
        String enterReservationPlace = scanner.nextLine().trim();
        userChoice[0] = enterReservationPlace.toLowerCase();

        System.out.print("Введите дату брони (yyyy-mm-dd): ");
        String enterReservationDate = scanner.nextLine().trim();
        userChoice[1] = enterReservationDate;

        System.out.print("Введите номер зала / места: ");
        String enterPlaceNumber = scanner.nextLine().trim();
        userChoice[2] = enterPlaceNumber;

        System.out.print("Введите номер слота: ");
        String enterStartTime = scanner.nextLine().trim();
        userChoice[3] = enterStartTime;

        return userChoice;
    }

    private Species choiceSpecies(String choice){
        Species userChoiceSpecies;
        if(choice.toLowerCase().matches("место")) {
            userChoiceSpecies = Species.WORKPLACE;
        } else if(choice.toLowerCase().matches("зал")) {
            userChoiceSpecies = Species.HALL;
        } else {
            throw new ReserveControllerException("Вы ввели ресурс не верно (повторите: 'зал' или 'место')!");
        }
        return userChoiceSpecies;
    }
}
