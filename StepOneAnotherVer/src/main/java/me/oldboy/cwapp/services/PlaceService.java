package me.oldboy.cwapp.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.entity.Species;
import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.exception.service_exception.PlaceServiceException;
import me.oldboy.cwapp.store.repository.PlaceRepository;
import me.oldboy.cwapp.store.repository.ReservationRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final ReservationRepository reservationRepository;

    public Long addNewPlace(Place place) {
        Long generateNewPlaceId = null;
        if(placeRepository.findBySpeciesAndPlaceNumber(place.getSpecies(), place.getPlaceNumber()).isEmpty()){
            generateNewPlaceId = placeRepository.create(place);
        } else {
            throw new PlaceServiceException("Создание дубликата рабочего места в БД запрещено!");
        }
        return generateNewPlaceId;
    }

    public List<Place> getAllPlaces() {
        List<Place> isPlaceList = placeRepository.findAll();
        if(isPlaceList.isEmpty()){
            throw new PlaceServiceException("База рабочих мест и залов пуста!");
        } else
            return isPlaceList;
    }

    public Place getPlaceBySpeciesAndPlaceNumber(Species species, Integer placeNumber) {
        Optional<Place> isPlaceExist =
                placeRepository.findBySpeciesAndPlaceNumber(species, placeNumber);
        if(isPlaceExist.isEmpty()){
            throw new PlaceServiceException(species + " с ID - " + placeNumber + " не существует!");
        } else
            return isPlaceExist.get();
    }

    public Place getPlaceById(Long placeId) {
        Optional<Place> isPlaceExist =
                placeRepository.findById(placeId);
        if(isPlaceExist.isEmpty()){
            throw new PlaceServiceException("Рабочее место/зал с ID - " + placeId + " не найден!");
        } else
            return isPlaceExist.get();
    }

    public List<Place> getPlaceBySpecies(Species species) {
        List<Place> isPlacesListExist =
                placeRepository.findAllBySpecies(species);
        if(isPlacesListExist.isEmpty()){
            throw new PlaceServiceException("В базе отсутствуют записи по любым: " + species + " !");
        } else
            return isPlacesListExist;
    }

    public Place updatePlace(Place placeForUpdate) {
        if(placeRepository.findById(placeForUpdate.getPlaceId()).isEmpty()){
            throw new PlaceServiceException("ID - " + placeForUpdate.getPlaceId() +
                                            " в базе не найден, обновление данных невозможно!");
        } else
            return placeRepository.update(placeForUpdate);
    }
    public boolean deletePlace(Long placeId) {
        Optional<List<Reservation>> isBasePresent = reservationRepository.findByPlaceId(placeId);
        if(placeRepository.findById(placeId).isEmpty()){
            throw new PlaceServiceException("Удаление несуществующего места/зала невозможно!");
        } else if(isBasePresent.isPresent() && isBasePresent.get().size() != 0){
                throw new PlaceServiceException("Удаление зарезервированного места/зала невозможно!");
        }
        return placeRepository.delete(placeId);
    }

    public boolean isPlaceExist(Species species, Integer placeNumber) {
        Optional<Place> isPlaceExist =
                placeRepository.findBySpeciesAndPlaceNumber(species, placeNumber);
        if(isPlaceExist.isEmpty()){
            return false;
        } else
            return true;
    }

    public boolean isPlaceExist(Long placeId) {
        Optional<Place> isPlaceExist =
                placeRepository.findById(placeId);
        if(isPlaceExist.isEmpty()){
            return false;
        } else
            return true;
    }
}
