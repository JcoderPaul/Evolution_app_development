package me.oldboy.cwapp.core.repository.crud;

import me.oldboy.cwapp.core.entity.Place;
import me.oldboy.cwapp.core.entity.Species;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {
    /* CRUD - Create */
    Optional<Place> createPlace(Place place);
    /* CRUD - Read */
    Optional<Place> findPlaceById(Long placeId);
    List<Place> findAllPlaces();
    /* CRUD - Update */
    boolean updatePlace(Place place);
    /* CRUD - Delete */
    boolean deletePlace(Long placeId);
    /* Other possibly necessary methods */
    Optional<Place> findPlaceBySpeciesAndNumber(Species species, Integer placeNumber);
    Optional<List<Place>> findAllPlacesBySpecies(Species species);
}
