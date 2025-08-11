package me.oldboy.services;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.annotations.Measurable;
import me.oldboy.dto.places.PlaceCreateDeleteDto;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.place_exception.PlaceServiceException;
import me.oldboy.mapper.PlaceMapper;
import me.oldboy.models.entity.Place;
import me.oldboy.models.entity.options.Species;
import me.oldboy.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for place managing.
 */
@Service
@AllArgsConstructor
@NoArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    /**
     * Create (save) new Place to base.
     *
     * @param createDto the new place for creating
     * @return new created (and save to base) place ID
     */
    @Transactional
    @Measurable
    public Long create(PlaceCreateDeleteDto createDto) {
        if(!isPlaceExist(createDto.species(), createDto.placeNumber())) {
            Place forCreatePlace = PlaceMapper.INSTANCE.mapToEntity(createDto);
            return placeRepository.save(forCreatePlace).getPlaceId();
        } else {
            throw new PlaceServiceException(createDto.species().name() + " с " + createDto.placeNumber() + " уже существует!");
        }
    }

    /**
     * Delete (remove) Place from base by placeId.
     *
     * @param placeId the place's ID for delete
     * @return true if place existed and deleted
     * false if place not exist and can not be deleted
     */
    @Transactional
    @Measurable
    public boolean delete(Long placeId) {
        Optional<Place> mayBePlace = placeRepository.findById(placeId);
        mayBePlace.ifPresentOrElse(place -> placeRepository.delete(place),
                () -> { throw new PlaceServiceException("Рабочего места/зала с ID - " + placeId + " не существует!");}
        );
        return mayBePlace.isPresent();
    }

    /**
     * Update Place data.
     *
     * @param placeUpdateDto data for update
     * @return true if place existed and update success
     * false if place not exist and can not be updated
     */
    @Transactional
    @Measurable
    public boolean update(PlaceReadUpdateDto placeUpdateDto) {
        /* Проверяем наличие записи в БД для изменения и получаем его */
        Optional<Place> maybeUser = placeRepository.findById(placeUpdateDto.placeId());

        maybeUser.ifPresent(place -> {
            place.setSpecies(placeUpdateDto.species());
            place.setPlaceNumber(placeUpdateDto.placeNumber());
            placeRepository.save(place);
        });

        /* Подтверждаем обновление */
        return maybeUser.isPresent();
    }

    /**
     * Find place (PlaceReadDto) by ID.
     *
     * @param placeId place ID in DB
     * @return PlaceReadDto if find it
     */
    @Measurable
    public Optional<PlaceReadUpdateDto> findById(Long placeId) {
        return placeRepository.findById(placeId).
                map(PlaceMapper.INSTANCE::mapToPlaceReadDto);
    }

    /**
     * Find all places from DB (PlaceReadDto).
     *
     * @return List of PlaceReadDto
     */
    @Measurable
    public List<PlaceReadUpdateDto> findAll() {
        return placeRepository
                .findAll()
                .stream()
                .map(PlaceMapper.INSTANCE::mapToPlaceReadDto)
                .collect(Collectors.toList());
    }

    /**
     * Find place (PlaceReadDto) by species (HALL / WORKPLACE) and number.
     *
     * @param species     detail (HALL / WORKPLACE)
     * @param placeNumber number of place (not ID)
     * @return PlaceReadDto if find it
     */
    @Measurable
    public Optional<PlaceReadUpdateDto> findPlaceBySpeciesAndNumber(Species species, Integer placeNumber) {
        Optional<Place> mayBePlace = placeRepository.findBySpeciesAndNumber(species.name(), placeNumber);
        if (mayBePlace.isEmpty()) {
            throw new PlaceServiceException("'" + species.name() + "' - с номером '" + placeNumber + "' не существует!");
        } else
            return Optional.ofNullable(PlaceMapper.INSTANCE.mapToPlaceReadDto(mayBePlace.get()));
    }

    /**
     * Find place (PlaceReadDto) by species (HALL / WORKPLACE).
     *
     * @param species detail (HALL / WORKPLACE)
     * @return List of place (PlaceReadDto) by one concrete species
     */
    @Measurable
    public List<PlaceReadUpdateDto> findAllPlacesBySpecies(Species species) {
        Optional<List<Place>> mayBeList =placeRepository.findAllBySpecies(species.name());
        if (mayBeList.isEmpty() || mayBeList.get().size() == 0) {
            throw new PlaceServiceException("Списка для '" + species.name() + "' не существует!");
        } else
            return mayBeList.get().stream()
                    .map(place -> PlaceMapper.INSTANCE.mapToPlaceReadDto(place))
                    .collect(Collectors.toList());
    }

    /**
     * Check exist place or not by two param
     *
     * @param species place species
     * @param placeNumber place number
     * @return true - place exist, false - place non existent
     */
    @Measurable
    public boolean isPlaceExist(Species species, Integer placeNumber) {
        Optional<Place> isPlaceExist = placeRepository.findBySpeciesAndNumber(species.name(), placeNumber);
        if (isPlaceExist.isPresent()) {
            return true;
        } else
            return false;
    }

    /**
     * Check exist place or not by one param
     *
     * @param placeId place id for checking
     * @return true - place exist, false - place non existent
     */
    @Measurable
    public boolean isPlaceExist(Long placeId) {
        Optional<PlaceReadUpdateDto> isPlaceExist = findById(placeId);
        if (isPlaceExist.isEmpty()) {
            return false;
        } else
            return true;
    }
}