package me.oldboy.cwapp.core.service;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.core.entity.Place;
import me.oldboy.cwapp.exceptions.services.PlaceServiceException;
import me.oldboy.cwapp.core.entity.Reservation;
import me.oldboy.cwapp.core.entity.Species;
import me.oldboy.cwapp.core.repository.crud.PlaceRepository;
import me.oldboy.cwapp.core.repository.crud.ReservationRepository;

import java.util.List;
import java.util.Optional;

/**
 * Place service layer - getting pure entities.
 */
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Create (save) new Place to base.
     *
     * @param place the new place for creating
     *
     * @return new created (and save to base) place ID
     */
    public Long createPlace(Place place){
        if(placeRepository.findPlaceBySpeciesAndNumber(place.getSpecies(),
                                                       place.getPlaceNumber()).isPresent()){
            throw new PlaceServiceException("'" + place.getSpecies() + "' - " +
                                            place.getPlaceNumber() +
                                            " уже существует!");
        } else
            return placeRepository.createPlace(place).get().getPlaceId();
    }

    /**
     * Find place by ID.
     *
     * @param placeId place ID for finding
     *
     * @return Place find place
     */
    public Place findPlaceById(Long placeId){
        Optional<Place> mayBePlace = placeRepository.findPlaceById(placeId);
        if(mayBePlace.isEmpty()){
            throw new PlaceServiceException("Место/зал с ID - " + placeId + " не найден!");
        } else
            return mayBePlace.get();
    }

    /**
     * Find all places.
     *
     * @return List of all finding places
     */
    public List<Place> findAllPlaces(){
        List<Place> allPlaces = placeRepository.findAllPlaces();
        if(allPlaces.size() == 0){
            throw new PlaceServiceException("База мест и залов пуста!");
        } else
            return allPlaces;
    }

    /**
     * Update existing place.
     *
     * @param place for update
     *
     * @return true if delete is success
     *         false if delete if fail
     */
    public boolean updatePlace(Place place){
        if(placeRepository.findPlaceById(place.getPlaceId()).isEmpty()){
            throw new PlaceServiceException("'" + place.getSpecies() + "' - " +
                                            place.getPlaceNumber() +
                                            " нельзя обновить, т.к. место/зал не существует!");
        } else
            return placeRepository.updatePlace(place);
    }

    /**
     * Delete place.
     *
     * @param placeId place ID to deleting
     *
     * @return true if delete is success
     *         false if delete fail
     */
    public boolean deletePlace(Long placeId){
        Optional<List<Reservation>> thisPlaceReservation =
                reservationRepository.findReservationByPlaceId(placeId);
        if(placeRepository.findPlaceById(placeId).isEmpty()){
            throw new PlaceServiceException("Удаление несуществующего места/зала невозможно!");
        } else if(thisPlaceReservation.isPresent() && thisPlaceReservation.get().size() != 0){
            throw new PlaceServiceException("Удаление зарезервированного места/зала невозможно!");
        } else
            return placeRepository.deletePlace(placeId);
    }

    /**
     * Find place by species (HALL / WORKPLACE) and number.
     *
     * @param species detail (HALL / WORKPLACE)
     * @param placeNumber number of place (not ID)
     *
     * @return Place if find it
     */
    public Place findPlaceBySpeciesAndNumber(Species species, Integer placeNumber){
        Optional<Place> mayBePlace = placeRepository.findPlaceBySpeciesAndNumber(species, placeNumber);
        if(mayBePlace.isEmpty()) {
            throw new PlaceServiceException("'" + species.getStrName() + "' - " +
                                            placeNumber +
                                            " не существует!");
        } else
            return mayBePlace.get();
    }

    /**
     * Find place by species (HALL / WORKPLACE).
     *
     * @param species detail (HALL / WORKPLACE)
     *
     * @return List of place by one species
     */
    public List<Place> findAllPlacesBySpecies(Species species){
        Optional<List<Place>> mayBeList = placeRepository.findAllPlacesBySpecies(species);
        if(mayBeList.isEmpty()){
            throw new PlaceServiceException("Списка" + "'" + species.getStrName() + "' - " +
                                            " не существует!");
        } else
            return mayBeList.get();
    }

    public boolean isPlaceExist(Species species, Integer placeNumber) {
        Optional<Place> isPlaceExist =
                placeRepository.findPlaceBySpeciesAndNumber(species, placeNumber);
        if(isPlaceExist.isEmpty()){
            return false;
        } else
            return true;
    }

    public boolean isPlaceExist(Long placeId) {
        Optional<Place> isPlaceExist =
                placeRepository.findPlaceById(placeId);
        if(isPlaceExist.isEmpty()){
            return false;
        } else
            return true;
    }
}