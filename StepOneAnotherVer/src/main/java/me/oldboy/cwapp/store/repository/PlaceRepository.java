package me.oldboy.cwapp.store.repository;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Species;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {
    Long create(Place place);
    Place update(Place place);
    Optional<Place> findById(Long placeId);
    boolean delete(Long placeId);
    Optional<Place> findBySpeciesAndPlaceNumber(Species species, Integer placeNumber);
    List<Place> findAll();
    List<Place> findAllBySpecies(Species species);
}
