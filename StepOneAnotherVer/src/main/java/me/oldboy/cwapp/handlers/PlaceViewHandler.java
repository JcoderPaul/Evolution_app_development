package me.oldboy.cwapp.handlers;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Role;
import me.oldboy.cwapp.entity.Species;
import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.handlers_exception.PlaceViewHandlerException;
import me.oldboy.cwapp.services.PlaceService;
import me.oldboy.cwapp.services.ReservationService;
import me.oldboy.cwapp.services.UserService;

import java.util.Scanner;

@RequiredArgsConstructor
public class PlaceViewHandler {

    private final PlaceService placeService;
    private final ReservationService reservationService;
    private final UserService userService;

    public void showAllHallsAndWorkplaces(){
        placeService.getAllPlaces()
                .forEach(place ->
                    System.out.println("ID - " + place.getPlaceId() +
                                       " вид: '" + place.getSpecies().getStrName() +
                                       "' номер: " + place.getPlaceNumber()));
    }

    public Long createNewPlace(Scanner scanner, Long userId){
        Long newPlaceGenerateId = null;
        Species newSpecies = null;
        User mayBeAdmin = userService.getExistUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Вы можете создать конференц-зал или рабочее место! " +
                             "\nВведите 'зал' или 'место': ");
            String whatPlaceWeMake = scanner.nextLine();
            System.out.print("Введите номер зала/места: ");
            String whatPlaceNumberWeMake = scanner.nextLine();

            if(whatPlaceWeMake.matches("зал")){
                newSpecies = Species.HALL;
            } else if(whatPlaceWeMake.matches("место")) {
                newSpecies = Species.WORKPLACE;
            } else {
                throw new PlaceViewHandlerException("Неверно сделан запрос, введите либо зал, либо место!");
            }

            if(placeService.isPlaceExist(newSpecies, Integer.parseInt(whatPlaceNumberWeMake))) {
                throw new PlaceViewHandlerException("Невозможно создать дубликат '" + newSpecies.getStrName() + "' !");
            } else {
                newPlaceGenerateId =
                    placeService.addNewPlace(new Place(newSpecies, Integer.parseInt(whatPlaceNumberWeMake)));
            }
        } else {
            throw new PlaceViewHandlerException("У пользователя недостаточно прав!");
        }
        return newPlaceGenerateId;
    }

    public boolean deletePlace(Scanner scanner, Long userId){
        Boolean isPlaceDeleted = false;
        User mayBeAdmin = userService.getExistUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Выберите из списка место/зал которое планируете удалить: ");
            showAllHallsAndWorkplaces();
            System.out.print("\nВведите выбранный ID: ");
            String placeForDelete = scanner.nextLine();

            if(placeService.isPlaceExist(Long.parseLong(placeForDelete)) &&
                    reservationService.findReservationByPlaceId(Long.parseLong(placeForDelete)).isEmpty()){
                placeService.deletePlace(Long.parseLong(placeForDelete));
                isPlaceDeleted = true;
            } else {
                throw new PlaceViewHandlerException("Нельзя удалять зарезервированные места/залы");
            }
        } else {
            throw new PlaceViewHandlerException("У пользователя недостаточно прав!");
        }
        return isPlaceDeleted;
    }

    public Place updatePlace(Scanner scanner, Long userId){
        Place updatingPlace = null;
        User mayBeAdmin = userService.getExistUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Выберите из списка место/зал которое планируете изменить: ");
            showAllHallsAndWorkplaces();
            System.out.print("Введите выбранный ID для изменения: ");
            String idPlaceForUpdate = scanner.nextLine();
            System.out.print("Присвойте новый номер месту/залу: ");
            String newNumberPlaceForUpdate = scanner.nextLine();

            if(reservationService.findReservationByPlaceId(Long.parseLong(idPlaceForUpdate)).isEmpty()){
                Place updatePlace = placeService.getPlaceById(Long.parseLong(idPlaceForUpdate));
                updatePlace.setPlaceNumber(Integer.parseInt(newNumberPlaceForUpdate));
                updatingPlace = placeService.updatePlace(updatePlace);
            } else {
                throw new PlaceViewHandlerException("Нельзя обновлять данные по зарезервированному месту/залу!");
            }
        } else {
            throw new PlaceViewHandlerException("У пользователя недостаточно прав!");
        }
        return updatingPlace;
    }
}
