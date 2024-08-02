package me.oldboy.cwapp.input.controllers;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.exceptions.controllers.PlaceControllerException;
import me.oldboy.cwapp.input.entity.Place;
import me.oldboy.cwapp.input.entity.Role;
import me.oldboy.cwapp.input.entity.Species;
import me.oldboy.cwapp.input.entity.User;
import me.oldboy.cwapp.input.service.PlaceService;
import me.oldboy.cwapp.input.service.ReserveService;
import me.oldboy.cwapp.input.service.UserService;

import java.util.Scanner;

@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final ReserveService reservationService;
    private final UserService userService;

    /* С - CRUD создаем новое рабочее место / зал */

    /**
     * Create new place (HALL and PLACE).
     *
     * @param scanner input user data from keyboard
     * @param userId  user ID
     * @throws PlaceControllerException if the user made the wrong choice
     * @throws PlaceControllerException if the user try made duplicate place
     * @throws PlaceControllerException if the user have no permit
     *
     * @return new created place ID
     */
    public Long createNewPlace(Scanner scanner, Long userId){
        Long newPlaceGenerateId = null;
        Species newSpecies = null;
        User mayBeAdmin = userService.findUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Вы можете создать конференц-зал или рабочее место! " +
                             "\nВведите 'зал' или 'место': ");
            String whatPlaceWeMake = scanner.nextLine();
            System.out.print("Введите номер зала/места: ");
            String whatPlaceNumberWeMake = scanner.nextLine();

            if(whatPlaceWeMake.toLowerCase().matches("зал")){
                newSpecies = Species.HALL;
            } else if(whatPlaceWeMake.toLowerCase().matches("место")) {
                newSpecies = Species.WORKPLACE;
            } else {
                throw new PlaceControllerException("Неверно сделан запрос, введите " +
                                                   "либо 'зал', либо 'место'!");
            }

            if(placeService.isPlaceExist(newSpecies, Integer.parseInt(whatPlaceNumberWeMake))) {
                throw new PlaceControllerException("Невозможно создать дубликат '" +
                                                   newSpecies.getStrName() + "' !");
            } else {
                newPlaceGenerateId =
                        placeService.createPlace(new Place(newSpecies, Integer.parseInt(whatPlaceNumberWeMake)));
            }
        } else {
            throw new PlaceControllerException("У пользователя недостаточно прав!");
        }
        return newPlaceGenerateId;
    }

    /* R - CRUD читаем (получаем) рабочие места / залы */

    /**
     * Read existing place (HALL and WORKPLACE).
     *
     * @param placeId place ID for find
     * @throws PlaceControllerException if the place non exist
     *
     * @return reading place
     */
    public Place readPlaceById(Long placeId){
        Place mayBePlace = placeService.findPlaceById(placeId);
        if(mayBePlace == null) {
            throw new PlaceControllerException("Конференц-зала / рабочего места с ID: " + placeId + " не существует!");
        } else
            return mayBePlace;
    }

    /**
     * Show all available HALLs and WORKPLACEs.
     */
    public void showAllPlaces(){
        placeService.findAllPlaces()
                .forEach(place -> System.out.println("ID - " + place.getPlaceId() +
                        " вид: '" + place.getSpecies().getStrName() +
                        "' номер: " + place.getPlaceNumber()));
    }

    /* U - CRUD обновляем данные по рабочему месту / залу */

    /**
     * Update existent place (HALL and PLACE).
     *
     * @param scanner input user data from keyboard
     * @param userId  user ID
     * @throws PlaceControllerException if the user try to update reservation place
     * @throws PlaceControllerException if the user have no permit
     *
     * @return true - if update success
     *         false - if update fail
     */
    public boolean updatePlace(Scanner scanner, Long userId){
        Boolean updatingPlace = null;
        User mayBeAdmin = userService.findUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Выберите из списка место/зал которое планируете изменить: ");
            showAllPlaces();
            System.out.print("Введите выбранный ID для изменения: ");
            String idPlaceForUpdate = scanner.nextLine();
            System.out.print("Присвойте новый номер месту/залу: ");
            String newNumberPlaceForUpdate = scanner.nextLine();

            if(reservationService.findReservationByPlaceId(Long.parseLong(idPlaceForUpdate)).isEmpty()){
                Place updatePlace = placeService.findPlaceById(Long.parseLong(idPlaceForUpdate));
                updatePlace.setPlaceNumber(Integer.parseInt(newNumberPlaceForUpdate));
                updatingPlace = placeService.updatePlace(updatePlace);
            } else {
                throw new PlaceControllerException("Нельзя обновлять данные по зарезервированному месту/залу!");
            }
        } else {
            throw new PlaceControllerException("У пользователя недостаточно прав!");
        }
        return updatingPlace;
    }

    /* D - CRUD удаляем рабочее место / зал */

    /**
     * Delete existing place (HALL and WORKPLACE).
     *
     * @param scanner input user data from keyboard
     * @param userId  user ID
     * @throws PlaceControllerException if the user try to delete reservation place
     * @throws PlaceControllerException if the user have no permit
     *
     * @return true - if delete success
     *         false - if delete fail
     */
    public boolean deletePlace(Scanner scanner, Long userId){
        Boolean isPlaceDeleted = false;
        User mayBeAdmin = userService.findUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Выберите из списка место/зал которое планируете удалить: ");
            showAllPlaces();
            System.out.print("\nВведите выбранный ID: ");
            String placeForDelete = scanner.nextLine();

            if(placeService.isPlaceExist(Long.parseLong(placeForDelete)) &&
                    reservationService.findReservationByPlaceId(Long.parseLong(placeForDelete)).isEmpty()){
                isPlaceDeleted = placeService.deletePlace(Long.parseLong(placeForDelete));
            } else {
                throw new PlaceControllerException("Нельзя удалять зарезервированные места/залы");
            }
        } else {
            throw new PlaceControllerException("У пользователя недостаточно прав!");
        }
        return isPlaceDeleted;
    }
}
